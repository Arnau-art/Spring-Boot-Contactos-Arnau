document.addEventListener('DOMContentLoaded', () => {
    const raiz = document.documentElement;

    // Modo claro / oscuro
    const cambiarTema = () => {
        const nuevo = raiz.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
        raiz.setAttribute('data-theme', nuevo);
        try { localStorage.setItem('tema', nuevo); } catch (e) { /* sin almacenamiento */ }
    };
    document.querySelectorAll('[data-tema-toggle]').forEach((b) => b.addEventListener('click', cambiarTema));

    // Brillo de los botones siguiendo al ratón
    document.addEventListener('pointermove', (e) => {
        const boton = e.target.closest && e.target.closest('.btn');
        if (!boton) return;
        const r = boton.getBoundingClientRect();
        boton.style.setProperty('--mx', (e.clientX - r.left) + 'px');
        boton.style.setProperty('--my', (e.clientY - r.top) + 'px');
    });

    // Notificaciones flotantes
    const mostrarToast = (mensaje) => {
        let contenedor = document.querySelector('.toasts');
        if (!contenedor) {
            contenedor = document.createElement('div');
            contenedor.className = 'toasts';
            contenedor.setAttribute('role', 'status');
            document.body.appendChild(contenedor);
        }
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.textContent = mensaje;
        contenedor.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    };
    document.querySelectorAll('[data-exportar]').forEach((enlace) => {
        enlace.addEventListener('click', () => mostrarToast('Preparando la descarga del CSV…'));
    });

    // Menú móvil
    const toggle = document.querySelector('[data-menu-toggle]');
    const menu = document.querySelector('[data-menu]');
    if (toggle && menu) {
        toggle.addEventListener('click', () => {
            const abierto = menu.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', String(abierto));
        });
    }

    // Buscador en vivo del listado
    const filtro = document.querySelector('[data-filtro]');
    if (filtro) {
        const normalizar = (t) => t.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
        const filas = Array.from(document.querySelectorAll('.table tbody tr'));
        const textos = filas.map((fila) => normalizar(fila.textContent));
        const contador = document.querySelector('[data-contador]');
        const sinResultados = document.querySelector('[data-sin-resultados]');
        filtro.addEventListener('input', () => {
            const consulta = normalizar(filtro.value.trim());
            let visibles = 0;
            filas.forEach((fila, i) => {
                const coincide = textos[i].includes(consulta);
                fila.hidden = !coincide;
                if (coincide) visibles++;
            });
            if (contador) contador.textContent = String(visibles);
            if (sinResultados) sinResultados.hidden = visibles !== 0;
        });
    }

    // Diálogo de confirmación
    document.querySelectorAll('[data-abrir-dialogo]').forEach((boton) => {
        boton.addEventListener('click', () => {
            const dialogo = document.getElementById(boton.dataset.abrirDialogo);
            if (dialogo) dialogo.showModal();
        });
    });
    document.querySelectorAll('[data-cerrar-dialogo]').forEach((boton) => {
        boton.addEventListener('click', () => boton.closest('dialog').close());
    });
    document.querySelectorAll('dialog').forEach((dialogo) => {
        dialogo.addEventListener('click', (e) => { if (e.target === dialogo) dialogo.close(); });
    });

    // Estado de carga al enviar formularios
    document.querySelectorAll('form[data-cargando]').forEach((form) => {
        form.addEventListener('submit', () => {
            const boton = form.querySelector('button[type="submit"]');
            if (!boton) return;
            boton.dataset.textoOriginal = boton.textContent;
            boton.textContent = boton.dataset.textoCargando || 'Procesando…';
            boton.classList.add('is-loading');
            setTimeout(() => { boton.disabled = true; }, 0);
        });
    });
    window.addEventListener('pageshow', (e) => {
        if (!e.persisted) return;
        document.querySelectorAll('button.is-loading').forEach((boton) => {
            boton.disabled = false;
            boton.classList.remove('is-loading');
            if (boton.dataset.textoOriginal) boton.textContent = boton.dataset.textoOriginal;
        });
    });

    // Mensajes: botón para cerrar y cierre automático de los de éxito
    document.querySelectorAll('.alert').forEach((alerta) => {
        const cerrar = document.createElement('button');
        cerrar.type = 'button';
        cerrar.className = 'alert__cerrar';
        cerrar.setAttribute('aria-label', 'Cerrar mensaje');
        cerrar.textContent = '×';
        cerrar.addEventListener('click', () => alerta.remove());
        alerta.appendChild(cerrar);
        if (alerta.classList.contains('alert--success')) {
            setTimeout(() => alerta.remove(), 6000);
        }
    });
});