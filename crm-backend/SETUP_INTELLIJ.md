# Configuración de IntelliJ IDEA para el proyecto CRM Backend

## Problemas identificados y soluciones

### 1. Versión de Java
- **Problema**: El proyecto requiere Java 21, pero el pom.xml original especificaba Java 17
- **Solución**: Se actualizó el pom.xml para usar Java 21

### 2. Maven no instalado
- **Problema**: Maven no estaba instalado en el sistema
- **Solución**: Se está instalando Maven vía Homebrew

### 3. IntelliJ no reconoce el proyecto
- **Problema**: IntelliJ IDEA no reconoce el proyecto como proyecto Maven/Spring Boot
- **Solución**: Seguir los pasos a continuación

## Pasos para configurar IntelliJ IDEA

### Paso 1: Reimportar el proyecto Maven

1. Abre IntelliJ IDEA
2. Ve a: **File** → **Invalidate Caches** → **Invalidate and Restart**
3. Una vez reiniciado, abre la ventana de Maven:
   - **View** → **Tool Windows** → **Maven**
4. En la ventana de Maven, haz clic en el ícono de **Reload All Maven Projects** (ícono circular con flechas)

### Paso 2: Configurar el SDK del proyecto

1. Ve a: **File** → **Project Structure** (Cmd + ;)
2. En la sección **Project**:
   - **SDK**: Selecciona **corretto-21** (ya está configurado)
   - **Language level**: Selecciona **21 - Pattern matching for switch**
3. En la sección **Modules**:
   - Verifica que el módulo `crm-backend` esté presente
   - Si no existe, haz clic en **+** → **Import Module** → Selecciona el `pom.xml`

### Paso 3: Habilitar el procesamiento de anotaciones (para Lombok)

1. Ve a: **Preferences** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
2. Marca la opción: **Enable annotation processing**
3. Aplica los cambios

### Paso 4: Verificar la configuración de Maven

1. Ve a: **Preferences** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
2. Verifica que:
   - **Maven home path**: Apunte a la instalación de Maven (debería ser `/opt/homebrew/bin/mvn`)
   - **User settings file**: Use el archivo por defecto
   - **Local repository**: Use el repositorio por defecto

### Paso 5: Configurar Spring Boot

1. Ve a la clase principal: `CrmApplication.java`
2. Haz clic derecho en el archivo
3. Selecciona: **Run 'CrmApplication'**
4. IntelliJ debería crear automáticamente una configuración de ejecución

Si no funciona:
1. Ve a: **Run** → **Edit Configurations**
2. Haz clic en **+** → **Spring Boot**
3. Configura:
   - **Name**: CRM Application
   - **Main class**: `com.empresa.crm.CrmApplication`
   - **Use classpath of module**: `crm-backend`
   - **JRE**: corretto-21

## Verificación

### 1. Compilar el proyecto

Ejecuta en la terminal:
```bash
mvn clean compile
```

Si todo está bien, deberías ver: `BUILD SUCCESS`

### 2. Ejecutar la aplicación

Opción A - Desde IntelliJ:
- Haz clic derecho en `CrmApplication.java`
- Selecciona **Run 'CrmApplication'**

Opción B - Desde terminal:
```bash
mvn spring-boot:run
```

La aplicación debería iniciar en: `http://localhost:8080`

### 3. Verificar endpoints

Una vez iniciada la aplicación, verifica:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs
- Actuator Health: http://localhost:8080/actuator/health

## Problemas comunes

### Error: "Cannot resolve symbol 'SpringBootApplication'"

**Solución**:
1. Abre la ventana de Maven
2. Haz clic en **Reimport All Maven Projects**
3. Si persiste, ejecuta: **File** → **Invalidate Caches** → **Invalidate and Restart**

### Error: "Lombok annotations not working"

**Solución**:
1. Instala el plugin de Lombok:
   - **Preferences** → **Plugins** → Busca "Lombok" → Instala
2. Reinicia IntelliJ
3. Habilita el procesamiento de anotaciones (ver Paso 3)

### Error: "Cannot connect to database"

**Solución**:
1. Asegúrate de que PostgreSQL esté ejecutándose
2. Verifica las credenciales en `src/main/resources/application-dev.yml`
3. Si no tienes PostgreSQL, instálalo:
```bash
brew install postgresql
brew services start postgresql
```

### El proyecto sigue sin reconocerse

**Solución**:
1. Cierra IntelliJ IDEA
2. Elimina la carpeta `.idea` del proyecto
3. Elimina el archivo `.iml` si existe
4. Abre IntelliJ y vuelve a abrir el proyecto como **Maven Project**

## Comandos útiles

```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar aplicación
mvn spring-boot:run

# Empaquetar JAR
mvn clean package

# Ver dependencias
mvn dependency:tree

# Actualizar dependencias
mvn clean install -U
```

## Estructura del proyecto

```
crm-backend/
├── src/main/java/com/empresa/crm/
│   ├── CrmApplication.java (clase principal)
│   ├── shared/ (configuración compartida)
│   ├── clientes/ (módulo de clientes)
│   ├── productos/ (módulo de productos)
│   ├── ventas/ (módulo de ventas)
│   ├── pagos/ (módulo de pagos)
│   ├── cuentas/ (módulo de cuentas)
│   ├── seguridad/ (módulo de seguridad)
│   └── reportes/ (módulo de reportes)
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── pom.xml
```

## Contacto

Si los problemas persisten después de seguir estos pasos, revisa:
1. Los logs de IntelliJ: **Help** → **Show Log in Finder**
2. Los logs de Maven en la ventana de **Maven** → **Toggle 'Skip Tests' Mode**
3. La documentación oficial de Spring Boot: https://spring.io/guides
