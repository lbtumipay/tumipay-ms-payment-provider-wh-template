

<!-- Page 1 -->

# Estándares de Diseño de Base de Datos SQL

<!-- Page 2 -->

# Finalidad del Estándar
Los presentes estándares constituyen una guía para el diseño de bases de datos, cuyo
objetivo es establecer criterios homogéneos para la nomenclatura de los objetos que se
crean en la base de datos, así como para la correcta normalización de las tablas.
Esta guía no pretende cubrir la totalidad de los aspectos relacionados con el diseño y la
construcción de bases de datos. Su propósito es servir como referencia para los
desarrolladores que requieren modelar un sistema y definir su base de datos
correspondiente, abordando los principios fundamentales de la normalización hasta la
tercera forma normal (3FN).
# Alcance y campo de Aplicación
El presente documento está dirigido a los ingenieros de software que integran los equipos de
desarrollo, a los líderes técnicos, a los arquitectos de software que forman parte de las
distintas unidades de proyecto y, en general, a todo el personal involucrado directa o
indirectamente en proyectos de desarrollo de software.
Este documento establece lineamientos y estándares aplicables al diseño de bases de datos
dentro de la plataforma TumiPay, y cubre específicamente los siguientes aspectos:

 * Estándar para la definición de nombres de tablas en TumiPay.
 * Estándar para el uso de tipos de campos en bases de datos MySQL y PostgresQL.
 * Estándar para la definición de nombres de campos en TumiPay.
 * Estándar para la definición de nombres de objetos en la base de datos.
 * Estándar para la normalización de tablas en TumiPay.
# Definiciones

### Sistema de Administración de Base de Datos (Database Management System (DBMS))
Un Data Management System (DMS), o sistema de gestión de datos, es un sistema de
software cuya función principal es facilitar la administración, organización y control de una o
varias bases de datos, pudiendo estas ser de distintos tipos. Su propósito es integrar y
gestionar la información de manera centralizada, proporcionando los mecanismos necesarios
para el acceso, mantenimiento, seguridad y consistencia de los datos a través de un sistema
gestor especializado.

### Modelo Entidad Relación
El Modelo Entidad–Relación (MER) es una herramienta de modelado de datos que permite
representar de manera conceptual la estructura de una base de datos. Facilita la 
identificación y definición de las entidades, sus atributos y las relaciones existentes entre
ellas, sirviendo como base para el diseño lógico y físico de la base de datos.

### Lenguaje de Consulta Estructurado (Structured Query Language – SQL)
El Lenguaje de Consulta Estructurado (SQL) es un lenguaje estándar utilizado para definir,
almacenar, consultar y manipular información en bases de datos relacionales. En este
modelo, la información se organiza en estructuras tabulares compuestas por filas y
columnas, las cuales representan los distintos atributos de los datos y las relaciones
existentes entre ellos.
Mediante instrucciones SQL es posible realizar operaciones como la inserción, actualización,
eliminación, consulta y recuperación de datos. Adicionalmente, SQL permite administrar y
optimizar el rendimiento de la base de datos, así como gestionar aspectos relacionados con
la integridad, seguridad y mantenimiento de la información.

### Lenguaje de Definición de Datos (Data Definition Language – DDL)
El Lenguaje de Definición de Datos (DDL) es un subconjunto del lenguaje SQL que agrupa
los comandos utilizados para definir y administrar el esquema de una base de datos. Su
función principal es describir la estructura de los objetos de la base de datos y permitir su
creación, modificación o eliminación, sin intervenir directamente sobre los datos
almacenados.
El DDL se utiliza para gestionar elementos estructurales como tablas, índices, vistas,
funciones, procedimientos almacenados, paquetes y disparadores (triggers). Generalmente,
estos comandos son ejecutados por administradores de bases de datos o por procesos
controlados, y no por usuarios finales, quienes acceden a la información a través de
aplicaciones.
#### Comandos DDL
* CREATE: Permite crear la base de datos y sus objetos, tales como tablas, índices, vistas,
funciones, procedimientos almacenados, paquetes y disparadores (triggers).
* DROP: Se utiliza para eliminar objetos existentes de la base de datos.
* ALTER: Permite modificar la estructura de un objeto de la base de datos.
* TRUNCATE: Elimina todos los registros de una tabla y libera el espacio asignado, sin
generar operaciones de borrado fila por fila.
* COMMENT: Permite agregar comentarios al diccionario de datos para documentar objetos
de la base de datos.
* RENAME: Se utiliza para cambiar el nombre de un objeto existente en la base de datos.
### Lenguaje de Manipulación de Datos (Data Manipulation Language – DML)
El Lenguaje de Manipulación de Datos (DML) corresponde al conjunto de comandos SQL
utilizados para acceder y manipular los datos almacenados en una base de datos. Estas
sentencias permiten insertar, actualizar, eliminar y consultar información, constituyendo la
base de la interacción operativa con los datos.
El DML controla el acceso a los datos y a las operaciones que se realizan sobre ellos. En
muchos sistemas de gestión de bases de datos, las sentencias de control de acceso (DCL)
se consideran estrechamente relacionadas con el DML, ya que regulan los permisos y
privilegios necesarios para ejecutar estas operaciones.
#### Comandos DML
* INSERT: Se utiliza para insertar nuevos registros en una tabla.
* UPDATE: Permite actualizar los datos existentes dentro de una tabla.
* DELETE: Se utiliza para eliminar registros de una tabla de la base de datos.
* LOCK: Permite controlar la concurrencia mediante el bloqueo explícito de tablas o
registros.
* CALL: Se utiliza para invocar subprogramas, como procedimientos almacenados en
PL/SQL o Java.
* EXPLAIN PLAN: Permite describir la ruta de acceso a los datos y analizar el plan de
ejecución de una consulta.

### Lenguaje de Control de Datos (Data Control Language – DCL)
El Lenguaje de Control de Datos (DCL) es el conjunto de comandos SQL destinados a
gestionar los derechos de acceso, permisos y controles de seguridad dentro de un sistema
de gestión de bases de datos. Estos comandos permiten definir qué usuarios o roles pueden
acceder a los objetos de la base de datos y qué operaciones están autorizados a realizar.
El DCL es un componente fundamental para garantizar la seguridad, la integridad y el control
de acceso a la información, y suele ser utilizado por administradores de bases de datos o por
procesos de gestión controlados.
#### Comandos DCL
* GRANT: Permite otorgar a usuarios o roles privilegios de acceso y operación sobre objetos
de la base de datos.
* REVOKE: Permite retirar o revocar los privilegios previamente otorgados mediante el
comando GRANT.

### Lenguaje de Control de Transacciones (Transaction Control Language – TCL)
El Lenguaje de Control de Transacciones (TCL) agrupa los comandos SQL utilizados para
gestionar y controlar la ejecución de transacciones dentro de una base de datos. Una transacción representa una unidad lógica de trabajo compuesta por una o varias operaciones
que deben ejecutarse de forma completa y consistente.
Cada transacción inicia con una operación específica y finaliza cuando todas las acciones
incluidas se completan correctamente. En caso de que alguna de ellas falle, la transacción
puede revertirse, garantizando la integridad de los datos. Por lo tanto, una transacción solo
puede tener dos estados finales: confirmada o revertida.
#### Comandos TCL
* COMMIT: Confirma y hace permanentes los cambios realizados durante una transacción.
* ROLLBACK: Revierte los cambios realizados en una transacción en caso de error o
cancelación.
* SAVEPOINT: Define un punto de guardado dentro de una transacción al cual se puede
regresar parcialmente.
* SET TRANSACTION: Permite definir las características de una transacción, como el nivel
de aislamiento y otras propiedades de control.

### Normalización de bases de datos
La normalización es el proceso mediante el cual se organizan los datos dentro de una base
de datos con el objetivo de reducir la redundancia, mejorar la integridad de la información y
facilitar su mantenimiento. Este proceso implica la correcta definición de tablas y el
establecimiento de relaciones entre ellas, siguiendo un conjunto de reglas diseñadas para
garantizar la consistencia y flexibilidad del modelo de datos.
La existencia de datos redundantes no solo desperdicia espacio de almacenamiento, sino
que también incrementa la complejidad operativa y el riesgo de inconsistencias. Cuando la
misma información se almacena en múltiples ubicaciones, cualquier modificación debe
replicarse de forma exacta en todos los puntos, aumentando la probabilidad de errores. Por
ejemplo, la actualización de la dirección de un cliente resulta más eficiente y segura cuando
dicha información se gestiona únicamente en la tabla de Clientes, en lugar de duplicarse en
otras tablas del sistema.


# Estándar para Nombres de Tablas y Campos
En este apartado se definen los estándares para el nombramiento de tablas y campos en las
bases de datos MySQL y PostgreSQL utilizadas en TumiPay.

La correcta definición de nombres en el diseño de bases de datos es un elemento clave para
garantizar la legibilidad, mantenibilidad y consistencia del modelo de datos. Los nombres de tablas y campos permiten comprender de forma clara la estructura y el propósito de la
información, facilitando el trabajo de los equipos de desarrollo, arquitectura y operación.

Por ello, el establecimiento de estándares claros y homogéneos para la nomenclatura de
tablas y campos es un componente esencial en el diseño y evolución de las bases de datos
de TumiPay.

En TumiPay se definen tres subtipos de tablas desde el punto de vista del diseño de
software y del modelo de datos:

## Tablas Principales
Las tablas principales contienen la información central y más relevante de la base de datos.
Generalmente representan las entidades principales del dominio del negocio y constituyen el
núcleo de la solución.

Ejemplo: tablas que almacenan información de cuentas, tarjetas y clientes en un sistema de
gestión de pagos.

## Tablas Transaccionales
Las tablas transaccionales almacenan la información asociada a operaciones y movimientos
del negocio, en particular aquellas relacionadas con transacciones monetarias. Estas tablas
suelen crecer de forma continua y pueden incluir información histórica.

Ejemplo: tablas de transacciones y de históricos de transacciones en un sistema de gestión
de pagos.

## Tablas de Referencia
Las tablas de referencia almacenan información estándar utilizada para clasificar, normalizar
y validar los datos de las tablas principales y transaccionales. Su función es garantizar
consistencia, integridad y reutilización de valores comunes dentro del modelo de datos.

Ejemplo: tablas de std_currencies, std_countries y std_cities.
Por lo general, estas tablas suelen estar asociadas a estructuras de tipo Enum en el código
fuente.

## Nombres de Tablas
Los nombres de las tablas deben definirse en minúsculas y construirse a partir de una
estructura compuesta por al menos dos atributos, separados mediante un carácter
separador, el cual puede utilizarse en más de una ocasión dentro del nombre. Esta convención permite identificar de forma clara, consistente y rápida el contenido y el
propósito de cada tabla dentro del modelo de datos.

A continuación, se detalla la estructura que debe seguirse para el nombramiento de tablas,
respetando estrictamente el orden en el que se describen sus componentes.


| Nombre                         | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                           | Ejemplo                |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|------------------------|
| Siglas Nombre de la Base de Datos | El nombre de cada tabla debe iniciar con un prefijo que identifique la base de datos o el sistema al cual pertenece. Este prefijo permite diferenciar de forma clara los objetos dentro de entornos compartidos o esquemas reutilizados. <br><br>En el caso de sistemas transversales y de uso estándar, el prefijo debe ser `std`. <br><br>Para sistemas que se instalan o despliegan sobre un esquema existente, el prefijo debe corresponder a las siglas del sistema propietario de la tabla. <br><br>Ejemplos: <br>• std: Standard <br>• cp: Card Payment <br>• pg: Payment Gateway | Alfanumérico – Longitud Máxima: 3 bytes           | std, cp, pg           |
| Nombre                         | Este componente del nombre de la tabla debe corresponder a una palabra en inglés que represente de forma clara los datos que serán almacenados. El nombre debe describir la entidad o conjunto de entidades que conforman la tabla, manteniendo coherencia semántica con el dominio del sistema. <br><br>Los nombres de las tablas en MySQL y PostgreSQL deben tener una longitud total de entre 1 y 63 bytes. Considerando el uso de prefijos definidos por el estándar, la longitud disponible para el nombre descriptivo de la entidad es de hasta 58 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras, estas deben estar separadas por el separador guion bajo (“_”). <br><br>Ejemplos: <br>• transactions <br>• cards <br>• customers <br>• currencies <br>• countries | Alfanumérico, Mayúsculas – Longitud Máxima: 58 bytes | cards, customers, countries |

## Nombres de Campos

Los nombres de los campos deben definirse en minúsculas y construirse a partir de una estructura compuesta por al menos dos atributos, separados mediante un carácter separador, el cual puede utilizarse en más de una ocasión dentro del nombre. Esta convención permite identificar de forma clara y precisa la información que almacena cada campo, facilitando su comprensión y uso dentro del modelo de datos.

A continuación, se detalla la estructura que debe seguirse para el nombramiento de los campos, respetando estrictamente el orden en el que se describen sus componentes.

| Nombre                     | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                          | Ejemplo            |
|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|--------------------|
| Siglas Nombre de la Tabla  | Siglas del nombre de la tabla a la cual pertenece el campo. Este valor, en caso de que el nombre esté compuesto por una sola palabra, toma las dos primeras o las tres primeras letras del nombre. <br><br>NOTA: Se omiten las siglas del nombre de la base de datos. <br><br>Ejemplos: <br>• ca: std_cards <br>• cus: std_customers <br>• oc: std_onboarding_config | Alfanumérico – Longitud Máxima: 3 bytes          | ca, cus, oc        |
| Separador                  | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                    | _                  |
| Nombre                     | Hace referencia a una palabra que represente la información que se almacena en el campo. Los nombres de los campos deben tener entre 1 y 56 bytes de largo. Para el nombre, la longitud restante es de 56 bytes. <br><br>NOTA: En los casos donde el nombre del campo esté compuesto por varias palabras, estas deben estar separadas por el separador guion bajo (“_”). <br><br>Ejemplos: <br>• id <br>• uuid <br>• name <br>• code <br>• created_at | Alfanumérico, Mayúsculas – Longitud Máxima: 56 bytes | id, uuid, name, code |


## Nombres de Claves Primarias
Los nombres de las claves primarias deben nombrarse en minúscula con una estructura compuesta por al menos dos atributos y un separador (el separador puede aparecer en más de una oportunidad), los cuales permitirán reconocer de forma rápida y simple cual es la clave primaria de la tabla.

A continuación, se detalla la estructura que se debe seguir, respetando el orden en el cual se listan:

| Nombre                  | Descripción                                                                                                                                                                                                                                                                                                              | Formato                                           | Ejemplo        |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------------|
| Prefijo Clave Primaria  | Identifica de forma única el nombre de la restricción que define la clave primaria de una tabla. Permite reconocer inmediatamente que se trata de una clave primaria y facilita tareas de mantenimiento, migración, auditoría y análisis de errores.                                                                 | Alfanumérico – Longitud Máxima: 2 bytes           | pk             |
| Separador               | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                              | Constante “_”                                     | _              |
| Nombre Tabla            | Nombre de la tabla a la cual pertenece la clave primaria. Para el nombre de la tabla, la longitud restante es de 53 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras, estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• customer_phone <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 53 bytes | account_type   |

## Nombres de Claves Únicas 
Los nombres de las claves únicas deben nombrarse en minúscula con una estructura compuesta por al menos dos atributos y un separador (el separador puede aparecer en más de una oportunidad), los cuales permitirán reconocer de forma rápida y simple cual es la clave única de la tabla.

A continuación, se detalla la estructura que se debe seguir, respetando el orden en el cual se listan:

| Nombre             | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                           | Ejemplo          |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|------------------|
| Prefijo Clave Única | Identifica una restricción de unicidad sobre una o varias columnas de una tabla. Garantiza que no existan valores duplicados para el conjunto de columnas definido.                                                                                                                                                                         | Alfanumérico – Longitud Máxima: 2 bytes           | uk               |
| Separador          | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _                |
| Nombre Tabla       | Nombre de la tabla a la cual pertenece la clave única. Para el nombre de la tabla la longitud asignada es de 25 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• customer_phone <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 25 bytes | account_type     |
| Separador          | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _                |
| Nombre Campo       | Nombre del campo que se define como clave única. Para el nombre del campo la longitud asignada es de 20 bytes. <br><br>NOTA: En los casos donde el nombre del campo esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• uuid <br>• code | Alfanumérico, Mayúsculas – Longitud Máxima: 20 bytes | uuid, code       |

## Nombres de Claves Foráneas
Los nombres de las claves foráneas deben nombrarse en minúscula con una estructura compuesta por al menos tres atributos y dos separadores, los cuales permitirán reconocer de forma rápida y simple cual es la clave foránea asociada a una tabla.

A continuación, se detalla la estructura que se debe seguir, respetando el orden en el cual se listan:

| Nombre                    | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                           | Ejemplo        |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------------|
| Prefijo Clave Foránea     | Identifica una restricción de integridad referencial entre una tabla y otra. Permite conocer de forma explícita la relación entre tablas y facilita el análisis de dependencias.                                                                                                                                                           | Alfanumérico – Longitud Máxima: 2 bytes           | fk             |
| Separador                 | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Tabla Origen       | Nombre de la tabla en donde se crea el campo foráneo. Para el nombre de la tabla origen la longitud asignada es de 17 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• customer_phone <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 17 bytes | transactions   |
| Separador                 | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Campo              | Nombre del campo que se define como clave foránea. Para el nombre del campo la longitud asignada es de 17 bytes. <br><br>NOTA: En los casos donde el nombre del campo esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• account_id <br>• card_id | Alfanumérico, Mayúsculas – Longitud Máxima: 17 bytes | account_id, card_id |
| Nombre Tabla Referenciada | Nombre de la tabla referenciada donde se encuentra el registro único propietario de los datos de la clave foránea. Para el nombre de la tabla referenciada la longitud asignada es de 17 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 17 bytes | account        |

## Nombres de Índices Únicos
Los nombres de los índices únicos deben nombrarse en minúscula con una estructura compuesta por al menos tres atributos y dos separadores, los cuales permitirán reconocer de forma rápida y simple si una tabla cuenta con índice único de búsqueda.

A continuación, se detalla la estructura que se debe seguir, respetando el orden en el cual se listan:

| Nombre          | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                           | Ejemplo        |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------------|
| Prefijo Índice  | Identifica un índice no único creado para mejorar el rendimiento de consultas, filtros u ordenamientos sobre una o varias columnas.                                                                                                                                                                                                       | Alfanumérico – Longitud Máxima: 3 bytes           | idx            |
| Separador       | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Tabla    | Nombre de la tabla en donde se crea el índice no único. Para el nombre de la tabla origen la longitud asignada es de 20 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• customer_phone <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 20 bytes | transactions   |
| Separador       | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Campo    | Nombre del campo que se define como índice no único. Para el nombre del campo la longitud asignada es de 30 bytes. <br><br>NOTA: En los casos donde el nombre del campo esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• uuid <br><br>NOTA: En los casos donde el índice está compuesto por varias columnas estas deben estar separadas por el separador signo guion bajo (“_”). | Alfanumérico, Mayúsculas – Longitud Máxima: 30 bytes | uuid           |

## Nombres de Valores por Defecto
Los nombres de los valores por defecto deben nombrarse en minúscula con una estructura compuesta por al menos tres atributos y dos separadores, los cuales permitirán reconocer de forma rápida y simple si una campo de una tabla cuenta con un valor por defecto. Su uso es opcional y depende del motor de base de datos.

A continuación, se detalla la estructura que se debe seguir, respetando el orden en el cual se listan:

| Nombre           | Descripción                                                                                                                                                                                                                                                                                                                                 | Formato                                           | Ejemplo        |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|----------------|
| Prefijo DEFAULT  | Identifica el nombre lógico de una restricción de valor por defecto aplicada a una columna. Su uso es opcional y depende del motor de base de datos.                                                                                                                                                                                       | Alfanumérico – Longitud Máxima: 2 bytes           | df             |
| Separador        | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Tabla     | Nombre de la tabla en donde se crea el valor por defecto. Para el nombre de la tabla la longitud asignada es de 20 bytes. <br><br>NOTA: En los casos donde el nombre de la tabla esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• account_type | Alfanumérico, Mayúsculas – Longitud Máxima: 20 bytes | account_type   |
| Separador        | Valor constante del signo guion bajo (“_”).                                                                                                                                                                                                                                                                                                 | Constante “_”                                     | _              |
| Nombre Campo     | Nombre del campo donde se asignará el valor por defecto. Para el nombre del campo la longitud asignada es de 30 bytes. <br><br>NOTA: En los casos donde el nombre del campo esté compuesto por varias palabras estas deben estar separadas por el separador signo guion bajo (“_”). <br><br>Ejemplos: <br>• status <br>• state | Alfanumérico, Mayúsculas – Longitud Máxima: 30 bytes | status, state  |

## Orden de los Campos en una Tabla

En el siguiente apartado se explica cómo se deben ordenar los campos al momento de crear una tabla. En este sentido, el orden de los campos debe ser el siguiente:

1. El primer campo de la tabla corresponde al campo **ID** de la tabla, de acuerdo con el estándar establecido en el capítulo de Estándar para nombres de Tablas y Campos.

2. El segundo campo de la tabla corresponde al **UUID** único de la tabla, de acuerdo con el estándar establecido en el capítulo de Estándar para nombres de Tablas y Campos.

3. El tercer campo de la tabla corresponde al campo **CODE** si se trata de una tabla de referencia.

4. Se deben crear todos los campos de **claves foráneas** después del campo UUID o del campo CODE y antes de cualquier otro campo de la entidad de dominio a la cual pertenece la tabla.

5. Los últimos campos de la tabla corresponden a los campos de auditoría:
   - `created_at`
   - `updated_at`
   - `deleted_at`


# Estándar para el Uso de Tipos de Campos

Este estándar define los lineamientos oficiales para la selección y uso de los tipos de datos en las bases de datos de la organización, garantizando consistencia, precisión, portabilidad y alineación con las necesidades del negocio y los requerimientos regulatorios. Su objetivo es asegurar que los datos sean almacenados de forma eficiente, segura y semánticamente correcta, promoviendo buenas prácticas en el modelado de datos y facilitando la interoperabilidad entre distintos motores de base de datos, específicamente MySQL y PostgreSQL.

El cumplimiento de este estándar permite mejorar la calidad de la información, optimizar el rendimiento de las consultas, facilitar las labores de mantenimiento, auditoría y análisis de datos, y reducir riesgos asociados a errores de diseño, pérdida de precisión en cálculos críticos y ambigüedad en la interpretación de la información almacenada.

## Nombres de Valores por Defect

| Tipo              | Descripción                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | Formato                                  | Ejemplo                     |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|-----------------------------|
| VARCHAR           | El tipo de campo VARCHAR se utiliza para almacenar cadenas de caracteres de longitud variable. Es el tipo estándar para almacenar texto corto o medio cuyo tamaño puede variar. <br><br>**Escenarios de uso:** <br>• Almacenamiento de nombres, apellidos, alias, códigos, identificadores funcionales. <br>• Columnas de descripción corta o media. <br>• Campos de configuración textual que no excedan un tamaño controlado. <br><br>**Consideraciones de rendimiento:** <br>VARCHAR utiliza únicamente el espacio requerido por el valor almacenado, lo que optimiza el uso de almacenamiento frente a tipos de longitud fija como CHAR. | VARCHAR(n) <br>donde n representa la longitud máxima en caracteres. | VARCHAR(50), VARCHAR(255)  |
| NUMERIC / DECIMAL | El tipo de campo NUMERIC (DECIMAL en MySQL) se utiliza para almacenar números enteros o decimales con precisión exacta. Es el tipo estándar para valores financieros y cálculos críticos. <br><br>**Escenarios de uso:** <br>• Almacenamiento de valores monetarios. <br>• Almacenamiento de porcentajes. <br>• Almacenamiento de montos transaccionales. <br><br>**Identificadores:** <br>Los identificadores numéricos deben ser NUMERIC(38,0).                                                                                                                                                | NUMERIC(precision, scale) <br>DECIMAL(precision, scale) | NUMERIC(38,0)               |
| UUID              | El tipo UUID se utiliza para almacenar identificadores únicos universales. Es el tipo estándar recomendado para claves primarias en sistemas distribuidos y microservicios. <br><br>**Escenarios de uso:** <br>• Claves primarias. <br>• Identificadores públicos. <br>• Integraciones entre sistemas. <br><br>**Ventajas:** <br>• Evita colisiones entre sistemas. <br>• Facilita sincronización y replicación. <br>• No expone secuencias internas.                                                                                                                                            | PostgreSQL: UUID <br>MySQL: CHAR(36)       | UUID                        |
| TIMESTAMP         | El tipo TIMESTAMP se utiliza para almacenar fecha y hora con precisión de segundos o fracciones de segundo. Es el tipo estándar para el registro de eventos y auditoría. <br><br>**Escenarios de uso:** <br>• Registro de eventos transaccionales. <br>• Auditoría de creación y modificación de registros. <br>• Sincronización de datos entre sistemas. <br>• Control de trazabilidad temporal. <br><br>**Recomendación:** <br>Usar siempre UTC.                                                                                                                                                | PostgreSQL: TIMESTAMP(n) <br>MySQL: TIMESTAMP(n) | TIMESTAMP, TIMESTAMP(6)     |
| DATE              | El tipo DATE se utiliza para almacenar fechas sin información de hora. Se emplea cuando la hora no es relevante para el negocio. <br><br>**Escenarios de uso:** <br>• Fechas de nacimiento. <br>• Fechas de emisión de documentos. <br>• Fechas contractuales. <br>• Fechas de vigencia.                                                                                                                                                                                                                                       | DATE                                      | DATE                        |
| TEXT              | El tipo TEXT se utiliza para almacenar grandes volúmenes de texto sin una longitud máxima estricta definida por el modelo de negocio. <br><br>**Escenarios de uso:** <br>• Descripciones extensas. <br>• Observaciones. <br>• Contenido textual no estructurado. <br>• Mensajes o payloads largos. <br><br>PostgreSQL: TEXT <br>MySQL: TEXT, MEDIUMTEXT, LONGTEXT                                                                                                                                                                                              | TEXT                                      | TEXT                        |
| JSONB / JSON      | El tipo de campo JSONB en PostgreSQL y JSON en MySQL se utiliza para almacenar datos semiestructurados en formato JSON, permitiendo flexibilidad en el modelo de datos cuando la estructura no es completamente fija o puede evolucionar en el tiempo. <br><br>**PostgreSQL (JSONB) almacena el contenido en formato binario optimizado, lo que permite:** <br>• Mejor rendimiento en consultas. <br>• Indexación eficiente (GIN / BTREE sobre expresiones). <br>• Eliminación de espacios y claves duplicadas. <br><br>**MySQL (JSON)** almacena el documento en un formato binario interno validado, permitiendo operaciones de consulta y extracción mediante funciones nativas (JSON_EXTRACT, ->, ->>). <br><br>**Escenarios de uso:** <br>• Almacenamiento de atributos dinámicos o extensibles. <br>• Configuraciones funcionales. <br>• Payloads de integración. <br>• Metadatos de auditoría. <br>• Información adicional no crítica para joins relacionales. <br><br>**Restricciones:** <br>• No debe usarse como reemplazo de un modelo relacional bien definido. <br>• No almacenar datos críticos de negocio que requieran integridad referencial estricta. <br>• Evitar estructuras excesivamente profundas. | PostgreSQL: JSONB <br>MySQL: JSON          | JSON, JSONB                 |


# Estándar para Normalización de Tablas
La normalización es el proceso de estructurar y organizar los datos dentro de una base de
datos relacional. Este proceso incluye la definición de tablas y el establecimiento de
relaciones entre ellas, siguiendo un conjunto de reglas cuyo objetivo principal es proteger la
integridad de los datos, reducir la redundancia y eliminar dependencias inconsistentes,
logrando así una base de datos más flexible, mantenible y eficiente.
Una dependencia incoherente se produce cuando se almacenan atributos de una entidad
de dominio en una tabla que no corresponde a dicha entidad. Por ejemplo, almacenar el
salario de un empleado en una tabla de usuarios constituye una dependencia incoherente,
ya que el atributo salario pertenece conceptualmente a la entidad Empleado y no a la entidad
Usuario. Este tipo de dependencias dificulta el acceso y la gestión de la información, dado
que la ruta lógica para localizar los datos puede ser confusa, incompleta o incorrecta.
La normalización de bases de datos se rige por un conjunto de reglas conocidas como
formas normales. Cada forma normal define un nivel específico de organización de los
datos:
Cuando una base de datos cumple la primera regla, se considera que está en Primera
Forma Normal (1FN).
Cuando cumple la primera y la segunda regla, se considera que está en Segunda Forma
Normal (2FN).
Cuando cumple la primera, segunda y tercera regla, se considera que está en Tercera
Forma Normal (3FN).
Cada forma normal representa un mayor grado de eliminación de redundancias y
dependencias no deseadas, contribuyendo a un diseño de base de datos más sólido y
consistente.
Aunque otros niveles de normalización son posibles, la tercera forma normal se considera el
nivel más alto necesario para la mayoría de las soluciones en TumiPay.

## Primera Forma Normal (1FN)

Una base de datos o una tabla se considera en Primera Forma Normal (1FN) cuando cumple
un conjunto de reglas orientadas a garantizar la atomicidad de los datos y la correcta
identificación de las entidades. Para ello, deben cumplirse las siguientes condiciones:

1. Eliminar grupos repetidos dentro de una misma tabla. Cada atributo debe contener un único valor y no listas, arreglos ni conjuntos de valores
repetidos.

2. Crear tablas independientes para cada conjunto de datos relacionados.
Los datos que representan diferentes conceptos o entidades deben separarse en tablas
distintas, evitando la mezcla de información no relacionada.
3. Identificar cada conjunto de datos relacionados mediante una clave primaria.
Cada tabla debe contar con una llave primaria que permita identificar de manera única
cada registro.
4. Asegurar que todos los atributos dependan de la clave primaria.
Todos los campos deben describir directamente a la entidad identificada por la llave
primaria. Además, debe existir independencia tanto del orden de las filas como del orden
de las columnas; es decir, el significado de los datos no debe verse afectado por cambios
en su disposición.
5. No permitir atributos con valores multivaluados.
Cada atributo debe almacenar un solo valor por registro, garantizando la atomicidad de la
información.

## Segunda Forma Normal (2FN)

Una base de datos o una tabla se considera en Segunda Forma Normal (2FN) cuando,
además de cumplir con los requisitos de la Primera Forma Normal, se eliminan las
dependencias parciales. Para ello, deben cumplirse las siguientes reglas:
1. Cumplir todas las reglas de la Primera Forma Normal (1FN).
La tabla debe garantizar atomicidad de los datos y ausencia de valores multivaluados.
2. Crear tablas independientes para conjuntos de atributos que se apliquen a múltiples
registros.
Los atributos que no dependen de la totalidad de la clave primaria, sino solo de una parte
de ella, deben separarse en tablas distintas.
3. Relacionar las tablas mediante claves foráneas.
Las nuevas tablas creadas deben vincularse con la tabla original utilizando claves
foráneas, asegurando la integridad referencial entre las entidades.
La Segunda Forma Normal busca asegurar que todos los atributos no clave dependan
completamente de la clave primaria, evitando redundancias derivadas de dependencias
parciales.

## Tercera Forma Normal (3FN)
Una base de datos o una tabla se considera en Tercera Forma Normal (3FN) cuando cumple
con los requisitos de la Primera y Segunda Forma Normal y, adicionalmente, elimina las
dependencias transitivas. Para ello, deben cumplirse las siguientes reglas:
1. Cumplir todas las reglas de la Primera Forma Normal (1FN).
2. Cumplir todas las reglas de la Segunda Forma Normal (2FN).
3. Eliminar los atributos que no dependan directamente de la clave primaria.
Los campos cuyo valor depende de otro atributo no clave, y no de la clave primaria, no
deben formar parte de la misma tabla.

En términos generales, si el contenido de un conjunto de campos puede aplicarse a más de
un registro de la tabla, dichos campos deben trasladarse a una tabla independiente,
estableciendo la relación correspondiente mediante una clave foránea. Esto garantiza que
cada tabla represente una única entidad y que todos sus atributos describan exclusivamente
a dicha entidad.

## Ejemplo de Normalización de Tablas

En el siguiente apartado se expone un ejemplo de normalización presentado por Microsoft
en el site Técnico de Microsoft, en este ejemplo se muestra el proceso de normalización de
una tabla de alumnos ficticia.
Paso # 1 Analizar las Tablas que se encuentren sin normalización
Se debe validar si los campos que componen la tabla cumplen con la Primera Forma Normal.
student_i tutor tutor_dispat class_1 class_2 class_3
d ch
1022 García 412 101-07 143-01 159-02
4123 Díaz 216 101-07 143-01 179-04
Paso # 2 Se aplican las reglas correspondientes a la Primera Forma Normal
Aplicando Regla # 1: Eliminar grupos repetidos.
Las tablas sólo deben tener dos dimensiones. Puesto que un alumno tiene varias clases,
estas clases deben aparecer en una tabla independiente. Los campos CLASS_1, CLASS_2 y
CLASS_3 de los registros anteriores son indicativos de un problema de diseño.

Otra forma de considerar ese problema es con una relación de uno a varios y poner el lado
de uno y el lado de varios en tablas distintas. En su lugar, se debe crear otra tabla en la
primera forma normal eliminando el grupo repetido CLASE_ID según se muestra a
continuación:

| student_i d | tutor | tutor_dispat ch | class_1 | class_2 | class_3 |
| --- | --- | --- | --- | --- | --- |
| 1022 | García | 412 | 101-07 | 143-01 | 159-02 |
| 4123 | Díaz | 216 | 101-07 | 143-01 | 179-04 |


| student_id | tutor | tutor_dispatch | class_id |
| --- | --- | --- | --- |
| 1022 | García | 412 | 101-07 |


| 1022 | García | 412 | 143-01 |
| --- | --- | --- | --- |
| 1022 | García | 412 | 159-02 |
| 4123 | Díaz | 216 | 101-07 |
| 4123 | Díaz | 216 | 143-01 |
| 4123 | Díaz | 216 | 179-04 |


| student_id | tutor_id | tutor_last_na me | tutor_dispatch | class_id |
| --- | --- | --- | --- | --- |
| 1022 | 1 | García | 412 | 101-07 |
| 1022 | 1 | García | 412 | 143-01 |
| 1022 | 1 | García | 412 | 159-02 |
| 4123 | 2 | Díaz | 216 | 101-07 |
| 4123 | 2 | Díaz | 216 | 143-01 |
| 4123 | 2 | Díaz | 216 | 179-04 |


Una vez la tabla se encuentra en la Primera Forma Normal se debe validar si los campos que
componen la tabla cumplen con la Segunda Forma Normal.
Los diversos valores de CLASE_ID para cada valor de STUDENT_ID en la tabla anterior.
CLASE_ID no depende funcionalmente de STUDENT_ID (la clave principal), de modo que la
relación no cumple la segunda forma normal.
Los diversos valores de TUTOR_LAST_NAME y TUTOR_DISPATCH para cada valor de
STUDENT_ID en la tabla anterior no dependen funcionalmente de STUDENT_ID (la clave
principal), de modo que la relación no cumple la segunda forma normal.
Las tablas siguientes muestran la segunda forma normal:

4123 179-04
Nota: Es importante destacar que es posible que una tabla requiera aplicar varias de las
reglas de la segunda forma normal, en el ejemplo solo está aplicando la regla “Eliminar
los datos redundantes”.
Nota: Es posible que deba ir aplicando cada una de las reglas por fases para ir
normalizando la tabla, el proceso de normalización es un proceso iterativo.

| student_id | tutor_id | tutor_last_name | tutor_dispatch |
| --- | --- | --- | --- |
| 1022 | 1 | Garcia | 412 |
| 4123 | 2 | Díaz | 216 |


| student_id | class_id |
| --- | --- |
| 1022 | 101-07 |
| 1022 | 143-01 |
| 1022 | 159-02 |
| 4123 | 101-07 |
| 4123 | 143-01 |
| 4123 | 179-04 |


<!-- Page 27 -->

Paso # 4 Se aplican las reglas correspondientes a la Tercera Forma Normal
Aplicando Regla # 3: Eliminar los campos que no dependan de la clave primaria.
Una vez la tabla se encuentra en la Segunda Forma Normal se debe validar si los campos
que componen la tabla cumplen con la Tercera Forma Normal.
En el último ejemplo, TUTOR_LAST_NAME (apellido del tutor) y TUTOR_DISPATCH (el
número de despacho del tutor) es funcionalmente dependiente del atributo TUTOR_ID. La
solución es pasar ese atributo de la tabla Alumnos a la tabla Personal, según se muestra a
continuación:

| student_id | tutor_id |
| --- | --- |
| 1022 | 1 |
| 4123 | 2 |


| tutor_id | tutor_last_name | tutor_dispatch |
| --- | --- | --- |
| 1 | Garcia | 412 |
| 2 | Díaz | 216 |


| student_i d | tutor | tutor_dispa tch | class_1 | class_2 | class_3 |
| --- | --- | --- | --- | --- | --- |
| 1022 | García | 412 | 101-07 | 143-01 | 159-02 |
| 4123 | Díaz | 216 | 101-07 | 143-01 | 179-04 |


| student_id | tutor_id |
| --- | --- |
| 1022 | 1 |
| 4123 | 2 |


| tutor_id | tutor_last_name | tutor_dispatch |
| --- | --- | --- |
| 1 | Garcia | 412 |
| 2 | Díaz | 216 |


| student_id | class_id |
| --- | --- |
| 1022 | 101-07 |
| 1022 | 143-01 |
| 1022 | 159-02 |
| 4123 | 101-07 |
| 4123 | 143-01 |
| 4123 | 179-04 |

