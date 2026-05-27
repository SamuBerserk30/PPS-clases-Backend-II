package co.edu.cesde.pps.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Carga variables desde el archivo .env a System properties antes de iniciar Spring Boot.
 *
 * <p>Se usa solo como ayuda para desarrollo local. Si una variable ya existe como
 * variable de entorno real o como system property, NO se sobrescribe.</p>
 */
public final class DotenvDevelopmentLoader {

    private static final Logger log = LoggerFactory.getLogger(DotenvDevelopmentLoader.class);

    private static final List<String> SUPPORTED_KEYS = List.of(
        "DB_HOST",
        "DB_PORT",
        "DB_NAME",
        "DB_USER",
        "DB_PASSWORD",
        "DB_DDL_AUTO",
        "DB_SHOW_SQL",
        "DB_POOL_SIZE",
        "APP_ENVIRONMENT",
        "LOG_LEVEL",
        "LOG_SQL_LEVEL",
        "LOG_SQL_BIND_LEVEL",
        "SPRING_PROFILES_ACTIVE"
    );

    private DotenvDevelopmentLoader() {
        throw new AssertionError("DotenvDevelopmentLoader is a utility class and cannot be instantiated");
    }

    public static void load() {
        try {
            String workingDir = System.getProperty("user.dir");
            log.info("Buscando .env en directorio: {}", workingDir);

            Dotenv dotenv = Dotenv.configure()
                .directory(workingDir)
                .filename(".env")
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

            int loadedProperties = 0;
            for (String key : SUPPORTED_KEYS) {
                loadedProperties += applyPropertyIfMissing(dotenv, key);
            }

            if (loadedProperties > 0) {
                log.info("Se cargaron {} propiedades desde .env en: {}", loadedProperties, workingDir);
            } else {
                log.warn("No se cargaron propiedades desde .env — verifica que el archivo exista en: {}", workingDir);
            }
        } catch (Exception exception) {
            log.warn("No se pudo cargar el archivo .env. La aplicación continuará con la configuración disponible: {}",
                exception.getMessage());
            log.debug("Detalle de la carga de .env", exception);
        }
    }

    private static int applyPropertyIfMissing(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            return 0;
        }
        if (System.getenv(key) != null || System.getProperty(key) != null) {
            return 0;
        }
        System.setProperty(key, value);
        return 1;
    }
}

