package com.example.contactos.controller;

import com.example.contactos.dto.ContactoForm;
import com.example.contactos.entity.Contacto;
import com.example.contactos.services.ContactoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/contactos")
public class ContactoController {

    private static final String VISTA_FORMULARIO = "contactos/formulario";
    private static final String REDIRECT_LISTA = "redirect:/contactos";

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contactos", contactoService.listar());
        return "contactos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("contactoForm", new ContactoForm());
        model.addAttribute("provincias", contactoService.listarProvincias());
        model.addAttribute("paises", contactoService.listarPaises());
        return VISTA_FORMULARIO;
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        String nombreArchivo = "contactos-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contactoService.exportarCsv());
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("contactoForm") ContactoForm form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        form.setId(null);
        validarReglasDeNegocio(form, result);
        if (result.hasErrors()) {
            model.addAttribute("provincias", contactoService.listarProvincias());
            return VISTA_FORMULARIO;
        }
        Contacto guardado = contactoService.guardar(form);
        redirect.addFlashAttribute("exito", "Contacto «" + guardado.getNombre() + "» creado correctamente.");
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        return contactoService.obtenerFormulario(id)
                .map(form -> {
                    model.addAttribute("contactoForm", form);
                    model.addAttribute("provincias", contactoService.listarProvincias());
                    model.addAttribute("paises", contactoService.listarPaises());
                    return VISTA_FORMULARIO;
                })
                .orElseGet(() -> noEncontrado(redirect));
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("contactoForm") ContactoForm form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        if (!contactoService.existe(id)) {
            return noEncontrado(redirect);
        }
        form.setId(id);
        validarReglasDeNegocio(form, result);
        if (result.hasErrors()) {
            model.addAttribute("provincias", contactoService.listarProvincias());
            model.addAttribute("paises", contactoService.listarPaises());
            return VISTA_FORMULARIO;

        }

        Contacto guardado = contactoService.guardar(form);
        redirect.addFlashAttribute("exito", "Contacto «" + guardado.getNombre() + "» actualizado correctamente.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        return contactoService.eliminar(id)
                .map(nombre -> {
                    redirect.addFlashAttribute("exito", "Contacto «" + nombre + "» eliminado correctamente.");
                    return REDIRECT_LISTA;
                })
                .orElseGet(() -> noEncontrado(redirect));
    }

    private void validarReglasDeNegocio(ContactoForm form, BindingResult result) {
        if (!result.hasFieldErrors("email") && contactoService.emailEnUso(form.getEmail(), form.getId())) {
            result.rejectValue("email", "email.duplicado", "El correo electrónico ya existe.");
        }
        if (!result.hasFieldErrors("provinciaId") && !contactoService.provinciaExiste(form.getProvinciaId())) {
            result.rejectValue("provinciaId", "provincia.invalida", "La provincia seleccionada no existe.");
        }
        if (!result.hasFieldErrors("paisId") && !contactoService.paisExiste(form.getPaisId())) {
            result.rejectValue("paisId", "pais.invalido", "El país seleccionado no existe.");
        }
    }

    private String noEncontrado(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El contacto solicitado no existe.");
        return REDIRECT_LISTA;
    }

}