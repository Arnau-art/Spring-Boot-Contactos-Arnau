# Agenda de contactos

Aplicación web para gestionar una agenda de contactos (nombre, teléfono, email y provincia). Está hecha con Spring Boot y Thymeleaf, guarda los datos en MySQL y exige iniciar sesión para usarla. En la interfaz se llama "Agenda Postal"; el proyecto Maven se llama `contactos`.

Empezó siendo una aplicación Symfony (PHP + Twig) y se ha reescrito en Java. La base de datos es la misma que usaba la versión de Symfony: esta aplicación no crea ni modifica tablas, se conecta a las que ya existen. Más abajo hay una sección con el esquema que espera encontrar.

Lo que se puede hacer:

- Iniciar y cerrar sesión.
- Ver el listado de contactos, buscar en él y exportarlo a CSV.
- Crear, editar y borrar contactos (el borrado se hace desde la pantalla de edición y pide confirmación).
- Crear usuarios nuevos desde la propia aplicación.
- Cambiar entre modo claro y oscuro.

## Tecnologías

| Tecnología | Para qué se usa aquí |
|---|---|
| Java 21 | Lenguaje. La versión está fijada en `pom.xml` (`java.version`). |
| Spring Boot 4.1.1 | Arranca la aplicación y la configura. Lleva Tomcat dentro, así que no hay que instalar ningún servidor. |
| Spring MVC (`spring-boot-starter-webmvc`) | Controladores y rutas. |
| Thymeleaf (`spring-boot-starter-thymeleaf`) | Genera el HTML en el servidor a partir de las plantillas de `templates/`. |
| Spring Data JPA (`spring-boot-starter-data-jpa`) | Acceso a la base de datos. Por debajo usa Hibernate. |
| MySQL Connector/J | Driver para hablar con MySQL. |
| Bean Validation (`spring-boot-starter-validation`) | Validación de los formularios con anotaciones. |
| Spring Security (`spring-boot-starter-security`) | Login, logout, protección CSRF y rutas privadas. |
| Maven | Compilación y dependencias. El proyecto incluye el wrapper (`mvnw`), no hace falta tener Maven instalado. |

El CSS (`static/css/styles.css`) y el JavaScript (`static/js/app.js`) están escritos a mano. No hay frameworks de frontend ni nada cargado desde un CDN.

## Qué necesitas para ejecutarlo

- JDK 21. Se comprueba con `java -version` y `javac -version`; los dos tienen que decir 21 o superior.
- Un MySQL en marcha con una base de datos llamada `contactos` y las tablas que se describen abajo.
- Nada más. Maven lo descarga el wrapper la primera vez.

## Base de datos

La conexión se configura en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/contactos
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.thymeleaf.cache=false
```

Ajusta usuario, contraseña y puerto a tu instalación. Para no dejar la contraseña escrita en el archivo (y que no acabe en el repositorio) puedes poner `spring.datasource.password=${DB_PASSWORD}` y arrancar con la variable de entorno: `DB_PASSWORD=tu_clave ./mvnw spring-boot:run`.

Las otras líneas importan:

- `ddl-auto=none` hace que Hibernate no toque el esquema. Las tablas las tienes que crear tú (o venir de la versión de Symfony). Si cambias una entidad y añades un campo, la columna hay que crearla a mano.
- `show-sql=true` imprime en consola cada consulta SQL. Va bien mientras desarrollas; quítalo si molesta.
- `thymeleaf.cache=false` es para desarrollo. En producción habría que quitarlo.

### Tablas que espera la aplicación

Si ya tienes la base de datos de la versión de Symfony, no hay que hacer nada. Si empiezas de cero, esto es lo que la aplicación espera encontrar (equivale a lo que generaba Doctrine):

```sql
CREATE TABLE provincia (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL
);

CREATE TABLE contacto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  telefono VARCHAR(15) NOT NULL,
  email VARCHAR(255) NOT NULL,
  provincia_id INT NOT NULL,
  FOREIGN KEY (provincia_id) REFERENCES provincia (id)
);

CREATE TABLE `user` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(180) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  roles JSON NOT NULL,
  password VARCHAR(255) NOT NULL
);
```

El desplegable de provincias sale de la tabla `provincia`, así que si está vacía no podrás crear contactos. Hay que rellenarla a mano.

### El primer usuario

Todas las páginas piden login, y la pantalla para crear usuarios también, así que el primer usuario hay que insertarlo directamente en MySQL. La contraseña tiene que ir ya cifrada con bcrypt. Una forma de sacar el hash es el comando de Symfony (`php bin/console security:hash-password`); sirve cualquier generador de bcrypt que produzca hashes que empiecen por `$2a$`, `$2b$` o `$2y$`.

```sql
INSERT INTO `user` (email, name, roles, password)
VALUES ('admin@ejemplo.com', 'Admin', '[]', 'PEGA_AQUI_EL_HASH');
```

El login se hace con el email y la contraseña. A partir de ahí, los usuarios siguientes se pueden crear desde el menú "Nuevo usuario".

## Arrancar el proyecto

Desde la carpeta del proyecto:

```
./mvnw spring-boot:run
```

Si te dice `permission denied`, haz antes `chmod +x mvnw`. La primera vez tarda porque descarga dependencias. Cuando veas una línea con `Started ContactosApplication`, abre `http://localhost:8080`: te llevará al login.

Para generar un jar ejecutable:

```
./mvnw package
java -jar target/contactos-0.0.1-SNAPSHOT.jar
```

El puerto es el 8080 (no hay ningún `server.port` configurado).

## Spring Boot para quien no lo ha usado nunca

Esta parte explica las ideas básicas del framework con ejemplos de este proyecto. Si ya conoces Spring, puedes saltar a "Estructura del proyecto".

### Qué es un framework, y qué es Spring Boot

Una librería es código que tú llamas cuando lo necesitas. Un framework funciona al revés: tú escribes piezas sueltas (un controlador, un servicio, una entidad) y es el framework quien decide cuándo ejecutarlas. En este proyecto no hay ningún sitio que llame a `ContactoController.listar()`; lo ejecuta Spring cuando llega un `GET /contactos`.

"Spring" es en realidad una familia de proyectos que se usan juntos:

- **Spring Framework** es el núcleo. Crea y conecta los objetos de la aplicación y trae Spring MVC, que es la parte que recibe peticiones web.
- **Spring Boot** va encima y quita trabajo de configuración. Arranca la aplicación con un servidor dentro (Tomcat) y configura solo lo que detecta en el proyecto.
- **Spring Data JPA** y **Spring Security** son módulos que se añaden cuando hacen falta: el primero para hablar con la base de datos, el segundo para el login.

Para añadir un módulo se usan los *starters*: una sola dependencia del `pom.xml` que arrastra todas las librerías necesarias. Por ejemplo, `spring-boot-starter-data-jpa` trae Hibernate y Spring Data JPA. Fíjate en que ninguna dependencia del `pom.xml` lleva número de versión: lo resuelve `spring-boot-starter-parent` (el `<parent>` del principio), que fija versiones compatibles entre sí. Para actualizar Spring Boot basta con cambiar el número de ese `<parent>` (ahora `4.1.1`).

### Qué pasa cuando arrancas la aplicación

Al ejecutar `./mvnw spring-boot:run`, lo que se lanza es el `main` de `ContactosApplication`. A partir de ahí, Spring hace más o menos esto:

1. Lee `application.properties` (conexión a MySQL, opciones de JPA y de Thymeleaf).
2. Recorre el paquete `com.example.contactos` y todos los que cuelgan de él buscando clases con anotaciones como `@Controller`, `@Service` o `@Configuration`.
3. Crea un objeto de cada una (se les llama *beans*) y los conecta entre sí. Para los repositorios, que son solo interfaces, genera él mismo la implementación.
4. Se conecta a MySQL, prepara Hibernate y monta la cadena de filtros de seguridad definida en `SecurityConfig`.
5. Arranca Tomcat en el puerto 8080 y se queda esperando peticiones. Es entonces cuando aparece en el log `Started ContactosApplication`.

Si una clase está fuera de `com.example.contactos` no se encuentra, no se crea y, si era un controlador, sus rutas dan 404.

### Inyección de dependencias

Es la idea que más desconcierta al principio. En lugar de crear los objetos que necesita con `new`, una clase los pide en su constructor y Spring se los entrega. Mira cómo empieza `ContactoController`:

```java
@Controller
@RequestMapping("/contactos")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }
    ...
}
```

Nadie hace `new ContactoController(...)` en el proyecto. Spring ve que el constructor necesita un `ContactoService`, busca el bean que lo es y se lo pasa. Con `ContactoService` pasa lo mismo: su constructor pide un `ContactoRepository` y un `ProvinciaRepository`.

También se pueden crear beans a mano con `@Bean`. Un caso real: `UsuarioService` necesita un `PasswordEncoder` para cifrar contraseñas. Ese objeto se declara una sola vez en `SecurityConfig` (el método `passwordEncoder()`, que devuelve un `BCryptPasswordEncoder`), y Spring lo inyecta tanto en `UsuarioService` como en el sistema de login. Hay una única instancia compartida, y si algún día quieres otro algoritmo de cifrado, lo cambias en ese único sitio.

### Las anotaciones

Casi todo lo que Spring sabe de tus clases lo sabe por las anotaciones (las palabras con `@`). Las que se usan en este proyecto:

| Anotación | Dónde aparece | Qué le dice a Spring |
|---|---|---|
| `@SpringBootApplication` | `ContactosApplication` | Es la clase de arranque; busca componentes a partir de su paquete. |
| `@Controller` | Controladores | Esta clase atiende peticiones web y devuelve vistas. |
| `@RequestMapping`, `@GetMapping`, `@PostMapping` | Métodos y clases de los controladores | Qué ruta y qué método HTTP atiende cada método. |
| `@PathVariable` | `ContactoController` | Coge un trozo de la URL (el `{id}`) como parámetro. |
| `@ModelAttribute` | `ContactoController`, `UsuarioController`, `GlobalModelAttributes` | En un parámetro: rellena un objeto con los campos del formulario. En un método: añade un valor al modelo de las vistas. |
| `@Valid` | Parámetros de los POST | Ejecuta las validaciones del objeto recibido. |
| `@ControllerAdvice` | `GlobalModelAttributes` | Lo que defina esta clase se aplica a todos los controladores. |
| `@Service` | `ContactoService`, `UsuarioService`, `UsuarioDetailsService` | Clase con lógica de negocio. |
| `@Transactional` | Métodos de los servicios | Todo el método se ejecuta en una transacción de base de datos. |
| `@Configuration`, `@EnableWebSecurity`, `@Bean` | `SecurityConfig` | Clase de configuración; cada `@Bean` es un objeto que Spring debe crear y guardar. |
| `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column` | Entidades | Esta clase es una tabla; qué columna es la clave, cuál se autogenera y cómo es cada campo. |
| `@ManyToOne`, `@JoinColumn` | `Contacto` | Relación con `Provincia` mediante la columna `provincia_id`. |
| `@EntityGraph` | `ContactoRepository` | Carga la provincia junto con los contactos en una sola consulta. |
| `@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@NotNull` | DTO | Reglas de validación de cada campo. |

### El patrón MVC

Spring MVC organiza la parte web en tres papeles:

- **Modelo**: los datos que se muestran o se reciben. Son las entidades (`Contacto`), los DTO (`ContactoForm`) y el objeto `Model` en el que el controlador deja lo que la vista necesita.
- **Vista**: lo que se ve. Aquí son las plantillas Thymeleaf de `templates/`.
- **Controlador**: recibe la petición, pide lo necesario a la capa de servicio y elige qué vista mostrar.

El reparto es estricto: el controlador no construye HTML y la plantilla no consulta la base de datos. Por ejemplo, `ContactoController.listar` deja la lista en el modelo con el nombre `contactos` y devuelve `"contactos/lista"`; la plantilla `lista.html` recorre `contactos` y la pinta. En este proyecto hay además una capa de servicio entre el controlador y los repositorios, que es donde está la lógica.

### De tablas a objetos (JPA)

Trabajar con una base de datos escribiendo SQL a mano es lo habitual en PHP puro. Aquí se hace con un ORM: cada fila de una tabla se convierte en un objeto Java. Una fila de `contacto` es un objeto `Contacto`, y viceversa. Hay tres nombres que se mezclan fácil:

- **JPA** es el estándar de Java que define cómo se mapean tablas a clases (las anotaciones `@Entity`, `@Column`…).
- **Hibernate** es la librería que lo implementa y la que genera el SQL de verdad.
- **Spring Data JPA** es la capa de Spring por encima, que genera los repositorios a partir de interfaces.

Así, `contactoRepository.save(contacto)` hace un `INSERT` si el contacto es nuevo (sin id) y un `UPDATE` si ya existía, y `findAll()` hace un `SELECT`. No escribes el SQL, pero puedes verlo en la consola porque `spring.jpa.show-sql=true`.

## Estructura del proyecto

Todo el código Java está bajo `com.example.contactos`:

```
src/main/java/com/example/contactos/
  ContactosApplication.java     clase con el main
  controller/
    ContactoController          listado, alta, edición, borrado y exportación
    UsuarioController           alta de usuarios
    LoginController             página de login y redirección de "/"
    GlobalModelAttributes       añade el nombre del usuario a todas las vistas
  dto/
    ContactoForm                datos del formulario de contacto + validaciones
    UsuarioForm                 datos del formulario de usuario + validaciones
  entity/
    Provincia, Contacto, User   clases mapeadas a las tablas
  repository/
    ProvinciaRepository, ContactoRepository, UserRepository
  service/
    ContactoService             lógica de contactos y generación del CSV
    UsuarioService              creación de usuarios
  security/
    SecurityConfig              reglas de acceso, login y logout
    UsuarioDetailsService       carga los usuarios de la tabla `user` para el login

src/main/resources/
  application.properties
  static/css/styles.css
  static/js/app.js
  templates/
    login.html
    error.html
    fragments/layout.html       cabecera, menú y mensajes compartidos
    contactos/lista.html
    contactos/formulario.html   sirve para crear y para editar
    usuarios/formulario.html
```

El reparto es el habitual: el controlador recibe la petición y decide qué vista mostrar, el servicio contiene la lógica, el repositorio habla con la base de datos y las entidades representan las tablas. Los DTO existen para no usar las entidades directamente en los formularios (se explica en la parte de validaciones).

## Cómo viaja una petición

Ejemplo con el listado, `GET /contactos`:

```
navegador → filtro de Spring Security → ContactoController.listar()
          → ContactoService.listar() → ContactoRepository.findAllByOrderByNombreAsc()
          → MySQL → (de vuelta) lista de Contacto
          → Model → vista contactos/lista → HTML → navegador
```

1. El navegador pide `/contactos`. Antes de llegar al controlador, Spring Security comprueba la sesión; si no hay, responde con una redirección a `/login`.
2. Spring MVC busca qué método atiende esa ruta: `ContactoController.listar`.
3. El controlador pide la lista al servicio, y este al repositorio. Hibernate genera el SQL y MySQL contesta.
4. El controlador guarda la lista en el `Model` con el nombre `contactos` y devuelve el texto `"contactos/lista"`.
5. Spring convierte ese texto en `templates/contactos/lista.html`, Thymeleaf lo rellena y se devuelve el HTML.
6. El navegador pide después `/css/styles.css` y `/js/app.js`, que Spring sirve desde `static/`.

## Spring MVC: los controladores

Estos son todos los controladores y las rutas que atienden:

| Método | Ruta | Controlador | Qué hace |
|---|---|---|---|
| GET | `/` | `LoginController` | Redirige a `/contactos`. |
| GET | `/login` | `LoginController` | Muestra el login (si ya hay sesión, redirige a `/contactos`). |
| POST | `/login` | Spring Security | Procesa el login. No hay método propio. |
| POST | `/logout` | Spring Security | Cierra la sesión. |
| GET | `/contactos` | `ContactoController` | Listado. |
| GET | `/contactos/exportar` | `ContactoController` | Descarga el CSV. |
| GET | `/contactos/nuevo` | `ContactoController` | Formulario vacío. |
| POST | `/contactos/nuevo` | `ContactoController` | Crea el contacto. |
| GET | `/contactos/{id}/editar` | `ContactoController` | Formulario relleno. |
| POST | `/contactos/{id}/editar` | `ContactoController` | Guarda los cambios. |
| POST | `/contactos/{id}/eliminar` | `ContactoController` | Borra el contacto. |
| GET | `/usuarios/nuevo` | `UsuarioController` | Formulario de usuario. |
| POST | `/usuarios/nuevo` | `UsuarioController` | Crea el usuario. |

Todas las clases son `@Controller` (no `@RestController`), lo que significa que lo que devuelven los métodos es el nombre de una vista, o una redirección con el prefijo `redirect:`. La excepción es `exportar()`, que devuelve un `ResponseEntity<byte[]>` con el CSV y no pasa por ninguna plantilla.

Algunas piezas que se repiten en `ContactoController` y conviene conocer:

- `@ModelAttribute("contactoForm")` hace que Spring rellene un `ContactoForm` con los campos del formulario enviado.
- `@Valid` junto a un `BindingResult` ejecuta las validaciones sin lanzar excepción: los errores quedan en el `BindingResult` y el controlador decide si vuelve a mostrar el formulario.
- Después de guardar o borrar se hace `redirect:/contactos`, y el mensaje se pasa con `RedirectAttributes.addFlashAttribute("exito", ...)`. Así, si el usuario recarga la página, no se reenvía el formulario. El atributo flash solo vive una petición.
- `GlobalModelAttributes` es un `@ControllerAdvice`: añade el atributo `usuarioActual` (el nombre del usuario) al modelo de todas las vistas, para pintarlo en el menú. Hace una consulta a `user` en cada petición; para el tamaño de esta aplicación no importa.

## Thymeleaf: las vistas

| Plantilla | Qué es |
|---|---|
| `login.html` | Formulario de login. Muestra los avisos de `?error` y `?logout`. |
| `error.html` | Página de error personalizada (Spring Boot la usa para los 404 y 500). |
| `fragments/layout.html` | Trozos compartidos, ver abajo. |
| `contactos/lista.html` | Tabla de contactos, buscador y botón de exportar. |
| `contactos/formulario.html` | Alta y edición de contacto. |
| `usuarios/formulario.html` | Alta de usuario. |

`layout.html` define cuatro fragmentos que las demás páginas incluyen con `th:replace`: `head(titulo)`, `navbar(activo)`, `mensajes` y `selectorTema`. El parámetro `activo` del menú es un texto (`'lista'`, `'nuevo'`, `'usuario'`) que sirve para marcar el enlace de la página actual.

Atributos de Thymeleaf que se usan:

- `th:each` para repetir las filas de la tabla, `th:if` para mostrar cosas según una condición, `th:text` para escribir valores (escapando el HTML).
- `th:href` y `th:action` con la sintaxis `@{/ruta}`. `th:action` en un formulario POST añade además el token CSRF, que Spring Security exige. Si escribes un formulario con `action="..."` normal, el POST dará 403.
- `th:object`, `th:field` y `th:errors` enlazan el formulario con `ContactoForm` o `UsuarioForm` y pintan el mensaje de error de cada campo.

El modelo que reciben las vistas: `contactos` (lista), `contactoForm` o `usuarioForm`, `provincias` (para el desplegable), `usuarioActual`, y los avisos `exito` y `error`.

Cosas que no son obvias:

- `contactos/formulario.html` es una sola plantilla para crear y para editar. Sabe en qué caso está porque `contactoForm.id` es nulo al crear. El botón de eliminar y el diálogo de confirmación solo se pintan si hay `id`.
- El formulario de borrado está dentro del `<dialog>`, fuera del formulario principal, porque en HTML no se pueden anidar formularios.
- Las versiones actuales de Thymeleaf ya no tienen el objeto `#request`. Por eso el nombre del usuario llega por `GlobalModelAttributes` y no se lee directamente en la plantilla.
- El tema claro/oscuro se decide con un pequeño script dentro de `<head>` (en `layout.html`) que pone `data-theme` en `<html>` antes de pintar la página. Si lo mueves al final del documento, se verá un parpadeo al cargar.
- El buscador del listado funciona solo en el navegador (`app.js` oculta filas); no hace ninguna petición al servidor.

## Spring Data JPA: cómo se accede a MySQL

Cada tabla tiene una entidad en `entity/`:

| Entidad | Tabla | Detalles |
|---|---|---|
| `Provincia` | `provincia` | `id` y `nombre`. |
| `Contacto` | `contacto` | `id`, `nombre`, `telefono`, `email` y `provincia`. La provincia es un `@ManyToOne` obligatorio (`provincia_id`), con carga diferida (`LAZY`). |
| `User` | `user` | `id`, `email`, `name`, `roles` y `password`. |

Las entidades no tienen setter para el `id`: lo genera MySQL (`GenerationType.IDENTITY`, es decir, el `AUTO_INCREMENT`).

Los repositorios son interfaces que extienden `JpaRepository`, que ya trae `findAll`, `findById`, `save`, `delete`, `existsById`, etc. Los métodos propios se declaran solo con el nombre y Spring construye la consulta a partir de él:

```java
public interface ContactoRepository extends JpaRepository<Contacto, Integer> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    @EntityGraph(attributePaths = "provincia")
    List<Contacto> findAllByOrderByNombreAsc();
}
```

`@EntityGraph` hace que el listado traiga cada contacto con su provincia en una sola consulta. Sin eso, la tabla haría una consulta extra por cada fila para sacar el nombre de la provincia.

`ProvinciaRepository` no tiene métodos propios y `UserRepository` tiene `findByEmail` y `existsByEmail`.

La lógica va en los servicios, con `@Transactional` (`readOnly = true` en las lecturas). Los controladores no usan los repositorios directamente, salvo `GlobalModelAttributes`, que consulta `UserRepository` para sacar el nombre.

## Spring Security

Toda la configuración está en `security/SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
    .anyRequest().authenticated())
.formLogin(form -> form
    .loginPage("/login")
    .defaultSuccessUrl("/contactos", true)
    .permitAll())
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout")
    .permitAll());
```

Es decir: solo el login, la página de error y los estáticos son públicos. Todo lo demás pide sesión, y si no la hay se redirige a `/login`. Tras entrar siempre se va a `/contactos`.

Cómo se comprueba un login:

- El formulario de `login.html` envía `username` y `password` a `POST /login`. En este proyecto el "username" es el **email**.
- `UsuarioDetailsService` busca el usuario con `UserRepository.findByEmail` y devuelve su email, su contraseña (el hash tal cual está en la base de datos) y sus permisos. Siempre se incluye `ROLE_USER`; además se añaden los que haya en la columna `roles`.
- La columna `roles` es un texto con JSON (`["ROLE_ADMIN"]`, o `[]`). Se interpreta quitando corchetes, comillas y espacios y separando por comas, sin librería JSON. Funciona para el formato que genera Symfony; si algún día `roles` tuviera algo más complejo, habría que cambiarlo.
- Spring compara la contraseña con el hash usando el `BCryptPasswordEncoder` declarado en `SecurityConfig`. Los hashes `$2y$` de Symfony son compatibles. Si en la tabla hubiera hashes de otro tipo (por ejemplo `$argon2...`), esos usuarios no podrán entrar con la configuración actual.

El cierre de sesión es un `POST` (hay un formulario en el menú), no un enlace. El CSRF está activo por defecto: cualquier formulario POST de la aplicación necesita el token, que Thymeleaf añade solo con `th:action`.

Sobre permisos: de momento no hay ninguna ruta restringida por rol. Cualquier usuario con sesión puede crear, editar y borrar contactos y crear otros usuarios. Los roles se cargan, pero nada los consulta. Si quieres restringir algo, por ejemplo la creación de usuarios, se añade en `SecurityConfig`, antes de `.anyRequest()`:

```java
.requestMatchers("/usuarios/**").hasRole("ADMIN")
```

y el usuario tiene que tener `["ROLE_ADMIN"]` en su columna `roles`.

## Validaciones

Hay dos niveles.

**Anotaciones en los DTO** (`ContactoForm` y `UsuarioForm`):

| Campo | Reglas |
|---|---|
| Contacto: `nombre` | Obligatorio, máximo 255. |
| Contacto: `telefono` | Obligatorio, entre 6 y 15 caracteres que sean números, `+`, espacios, paréntesis, puntos o guiones. |
| Contacto: `email` | Obligatorio, formato de email, máximo 255. |
| Contacto: `provinciaId` | Obligatorio (hay que elegir una). |
| Usuario: `name` | Obligatorio, máximo 255. |
| Usuario: `email` | Obligatorio, formato de email, máximo 180. |
| Usuario: `password` | Obligatoria, entre 8 y 72 caracteres. |
| Usuario: `confirmarPassword` | Obligatorio. |

El máximo de 72 en la contraseña no es capricho: bcrypt no admite más y Spring Security lanza una excepción si se supera.

Los setters de los DTO hacen `trim()` de nombre, teléfono y email, de modo que un campo con solo espacios cuenta como vacío.

**Reglas que se comprueban en el controlador**, porque necesitan consultar la base de datos o comparar dos campos (método `validarReglasDeNegocio` en `ContactoController` y comprobaciones equivalentes en `UsuarioController`):

- El email del contacto no puede repetirse. Al crear se usa `existsByEmail`; al editar, `existsByEmailAndIdNot`, para que un contacto pueda conservar su propio email.
- La provincia elegida tiene que existir.
- El email del usuario no puede repetirse.
- Las dos contraseñas tienen que coincidir.

Los errores se añaden con `result.rejectValue(...)` y salen en el campo correspondiente con `th:errors`.

Un par de decisiones que despistan:

- Se usan DTO en lugar de las entidades en los formularios porque el select de provincia envía un id (`provinciaId`), no un objeto `Provincia`, y para que el formulario solo pueda tocar los campos previstos.
- La unicidad del email de contacto se comprueba solo en la aplicación, igual que hacía `UniqueEntity` en Symfony. Si en tu tabla `contacto` no hay un índice único, la base de datos no lo impedirá por su cuenta.
- La validación del teléfono es más estricta que la de la versión de Symfony. Si hay contactos antiguos con un formato raro, al editarlos el formulario pedirá corregir el teléfono.

## Flujos principales

**Iniciar sesión.** `GET /login` pinta `login.html`. El formulario envía el email y la contraseña a `POST /login`; Spring Security carga el usuario con `UsuarioDetailsService`, comprueba el hash y, si es correcto, redirige a `/contactos`. Si falla, vuelve a `/login?error` y la plantilla muestra el aviso.

**Crear un contacto.**
1. `GET /contactos/nuevo`: `ContactoController.nuevo` pone un `ContactoForm` vacío y la lista de provincias (`ContactoService.listarProvincias`, ordenada por nombre) en el modelo y devuelve `contactos/formulario`.
2. El usuario envía el formulario a `POST /contactos/nuevo`. Spring rellena el `ContactoForm` y ejecuta las anotaciones de validación.
3. `validarReglasDeNegocio` comprueba email repetido y provincia existente.
4. Si hay errores, se vuelve a pintar el formulario con los mensajes. Si no, `ContactoService.guardar` crea la entidad `Contacto`, la rellena y la guarda.
5. Redirección a `/contactos` con el aviso "Contacto «…» creado correctamente."

**Editar un contacto.** Igual que crear, pero la ruta lleva el id. `ContactoService.obtenerFormulario(id)` convierte la entidad en un `ContactoForm` para rellenar el formulario. Si el id no existe, se redirige al listado con el aviso "El contacto solicitado no existe." Al guardar, `guardar` busca la entidad por el id del formulario y actualiza sus campos.

**Borrar un contacto.** En la pantalla de edición, el botón "Eliminar" abre un `<dialog>` de confirmación (lo abre `app.js`). Al confirmar se envía `POST /contactos/{id}/eliminar`. `ContactoService.eliminar` devuelve el nombre del contacto borrado, que se usa para el aviso, o vacío si no existía. El borrado es real, no hay papelera.

**Exportar a CSV.** El botón del listado apunta a `GET /contactos/exportar`. `ContactoService.exportarCsv` genera el texto y el controlador lo devuelve como descarga (`contactos-AAAA-MM-DD.csv`). El archivo va en UTF-8 con BOM, para que Excel respete las tildes, y con `;` como separador, que es lo que Excel espera en configuración regional española. Las celdas que empiezan por `=`, `+`, `-` o `@` llevan una comilla simple delante para que Excel no las interprete como fórmula, salvo los teléfonos con formato normal (`+34 600 123 456`).

**Crear un usuario.** `GET /usuarios/nuevo` muestra el formulario; `POST /usuarios/nuevo` valida, comprueba email repetido y contraseñas iguales, y `UsuarioService.crear` guarda el usuario con la contraseña cifrada con bcrypt y `roles` a `[]`.

## Cómo añadir o cambiar cosas

### Añadir un campo a los contactos

Como ejemplo, un campo `direccion`. Hay que tocar, en este orden:

1. **Base de datos**: crear la columna a mano, porque `ddl-auto=none`. Por ejemplo `ALTER TABLE contacto ADD direccion VARCHAR(255) NULL;`.
2. **`entity/Contacto.java`**: el atributo con `@Column(length = 255)` y su getter y setter.
3. **`dto/ContactoForm.java`**: el atributo, sus anotaciones de validación y getter y setter.
4. **`service/ContactoService.java`**: copiar el valor en los dos sentidos, en `obtenerFormulario` (entidad a formulario) y en `guardar` (formulario a entidad). Si quieres que salga en el CSV, añade la columna en `exportarCsv`.
5. **`templates/contactos/formulario.html`**: copiar el bloque `form-group` de otro campo y cambiar el nombre en `th:field` y `th:errors`.
6. **`templates/contactos/lista.html`**: añadir la columna en `<thead>` y en el `<tr th:each>`. Las celdas llevan `data-label` para la vista móvil; no te olvides de ponerlo.

### Añadir una página nueva

1. Un método en un controlador (o un controlador nuevo dentro de `controller/`) con su `@GetMapping`, que devuelva el nombre de una plantilla.
2. La plantilla en `templates/`, empezando por el mismo esqueleto que `contactos/lista.html` (el `th:replace` del `head` y del `navbar`).
3. Si quieres que salga en el menú, un enlace en `fragments/layout.html`. El valor que pases a `navbar('...')` en la página tiene que coincidir con el que compruebas en ese enlace (`th:classappend`).

No hay que tocar `SecurityConfig` para que sea privada: cualquier ruta nueva queda protegida por el `anyRequest().authenticated()`.

### Cambiar reglas de validación

Las anotaciones están en `ContactoForm` y `UsuarioForm`. Los mensajes están escritos directamente en cada anotación (`message = "..."`). Las reglas que consultan la base de datos están en el controlador, en `validarReglasDeNegocio`.

## Tests

```
./mvnw test
```

El `pom.xml` incluye las dependencias de test de Spring Boot (`spring-boot-starter-webmvc-test` y `spring-boot-starter-thymeleaf-test`), pero de momento no hay tests escritos para la lógica de la aplicación; solo el de arranque que genera Spring Initializr, si no se ha borrado. Ese test levanta el contexto completo, así que necesita MySQL en marcha y las credenciales de `application.properties` correctas. Sin base de datos, falla.

## Problemas habituales

**La aplicación no arranca y habla de `DataSource` o de `url`.** `application.properties` no está en `src/main/resources`, no se ha guardado, o le falta `spring.datasource.url`. Si el error es `Access denied` o un fallo de comunicación, es la contraseña o que MySQL no está arrancado.

**Una ruta nueva da 404.** Casi siempre es una de estas tres: la clase está fuera de `com.example.contactos`, el archivo se llama `.Java` en lugar de `.java` (en Linux las mayúsculas cuentan y Maven lo ignora), o no se reinició la aplicación tras crearla.

**`Error resolving template [...]`.** La plantilla no está donde se espera. `login.html`, `error.html` y la carpeta `fragments` van directamente en `templates/`; las de contactos y usuarios, en sus subcarpetas. Cuidado también con que la carpeta se llame `templates` (con s final).

**Cambios en plantillas, CSS o JS que no se ven.** El proyecto no usa devtools, así que lo más seguro es reiniciar la aplicación tras cambiar plantillas. Para el CSS y el JS, el navegador los guarda en caché: recarga con `Ctrl+F5`.

**No puedo iniciar sesión.** Comprueba que el usuario existe en la tabla `user` y que el hash de su contraseña empieza por `$2a$`, `$2b$` o `$2y$`. Con hashes de otro tipo no entrará. Recuerda que el "usuario" es el email.

**Los formularios POST dan 403.** Falta el token CSRF: el formulario tiene que usar `th:action`, no `action`.

**La consola va llena de SQL.** Es `spring.jpa.show-sql=true`. Cámbialo a `false` si molesta.

**El puerto 8080 está ocupado.** Mira qué lo usa con `ss -ltnp | grep 8080` o añade `server.port=8081` en `application.properties`.

**Al abrir el CSV en Excel todo aparece en una columna.** Pasa si Excel está configurado con coma como separador. Importa el archivo indicando `;` como delimitador.
