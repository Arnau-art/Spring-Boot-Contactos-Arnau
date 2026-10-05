package com.example.contactos.services;

import com.example.contactos.dto.ContactoForm;
import com.example.contactos.entity.Contacto;
import com.example.contactos.entity.Pais;
import com.example.contactos.entity.Provincia;
import com.example.contactos.repository.ContactoRepository;
import com.example.contactos.repository.PaisRepository;
import com.example.contactos.repository.ProvinciaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class ContactoService {

    private final ContactoRepository contactoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final PaisRepository paisRepository;

    public ContactoService(ContactoRepository contactoRepository,
            ProvinciaRepository provinciaRepository,
            PaisRepository paisRepository) {
        this.contactoRepository = contactoRepository;
        this.provinciaRepository = provinciaRepository;
        this.paisRepository = paisRepository;
    }

    @Transactional(readOnly = true)
    public List<Contacto> listar() {
        return contactoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Provincia> listarProvincias() {
        return provinciaRepository.findAll(Sort.by("nombre"));
    }

    @Transactional(readOnly = true)
    public List<Pais> listarPaises() {
        return paisRepository.findAll(Sort.by("nombre"));
    }

    @Transactional(readOnly = true)
    public boolean existe(Integer id) {
        return contactoRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean provinciaExiste(Integer id) {
        return provinciaRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean paisExiste(Integer id) {
        return id != null && paisRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean emailEnUso(String email, Integer idActual) {
        return idActual == null
                ? contactoRepository.existsByEmail(email)
                : contactoRepository.existsByEmailAndIdNot(email, idActual);
    }

    @Transactional(readOnly = true)
    public Optional<ContactoForm> obtenerFormulario(Integer id) {
        return contactoRepository.findById(id).map(c -> {
            ContactoForm form = new ContactoForm();
            form.setId(c.getId());
            form.setNombre(c.getNombre());
            form.setTelefono(c.getTelefono());
            form.setEmail(c.getEmail());
            form.setProvinciaId(c.getProvincia().getId());
            form.setPaisId(c.getPais() != null ? c.getPais().getId() : null);
            return form;
        });
    }

    @Transactional
    public Contacto guardar(ContactoForm form) {
        Contacto contacto = form.getId() == null
                ? new Contacto()
                : contactoRepository.findById(form.getId())
                        .orElseThrow(() -> new IllegalStateException("El contacto no existe."));
        contacto.setNombre(form.getNombre());
        contacto.setTelefono(form.getTelefono());
        contacto.setEmail(form.getEmail());
        contacto.setProvincia(provinciaRepository.getReferenceById(form.getProvinciaId()));
        contacto.setPais(paisRepository.getReferenceById(form.getPaisId()));
        return contactoRepository.save(contacto);
    }

    /** Devuelve el nombre del contacto eliminado, o vacío si no existía. */
    @Transactional
    public Optional<String> eliminar(Integer id) {
        return contactoRepository.findById(id).map(c -> {
            contactoRepository.delete(c);
            return c.getNombre();
        });
    }

    private static final Pattern TELEFONO_SEGURO = Pattern.compile("^\\+?[0-9][0-9()\\s.-]*$");

    @Transactional(readOnly = true)
    public byte[] exportarCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF"); // BOM para que Excel respete las tildes
        csv.append("Nombre;Teléfono;Email;Provincia;País\r\n");
        for (Contacto c : contactoRepository.findAllByOrderByNombreAsc()) {
            csv.append(celdaCsv(c.getNombre())).append(';')
                    .append(celdaCsv(c.getTelefono())).append(';')
                    .append(celdaCsv(c.getEmail())).append(';')
                    .append(celdaCsv(c.getProvincia().getNombre())).append(';')
                    .append(celdaCsv(c.getPais() != null ? c.getPais().getNombre() : "")).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String celdaCsv(String valor) {
        String texto = valor == null ? "" : valor;
        // Evita que Excel interprete el contenido como una fórmula (inyección CSV)
        if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0
                && !TELEFONO_SEGURO.matcher(texto).matches()) {
            texto = "'" + texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}