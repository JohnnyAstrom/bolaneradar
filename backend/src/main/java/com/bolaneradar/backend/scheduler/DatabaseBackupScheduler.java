//package com.bolaneradar.backend.scheduler;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDate;
//
///**
// * Skapar automatiskt både binär (.dump) och läsbar (.sql) säkerhetskopia
// * av PostgreSQL-databasen varje natt innan nya räntor hämtas.
// */
//@Component
//public class DatabaseBackupScheduler {
//
//    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupScheduler.class);
//
//    @Value("${spring.datasource.username}")
//    private String dbUser;
//
//    @Value("${spring.datasource.password}")
//    private String dbPassword;
//
//    @Value("${spring.datasource.url}")
//    private String dbUrl;
//
//    private static final String PG_DUMP_PATH = "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe";
//    private static final String BACKUP_DIR = "./backups";
//
//    /**
//     * Körs 08.45 varje dag (innan ScraperScheduler)
//     * Skapar både .dump och .sql backup.
//     */
//    @Scheduled(cron = "0 40 12 * * *")
//    public void backupDatabase() {
//        String dbName = extractDbName(dbUrl);
//        String host = extractHost(dbUrl);
//        LocalDate today = LocalDate.now();
//
//        String dumpFileName = String.format("%s/%s_%s.dump", BACKUP_DIR, dbName, today);
//        String sqlFileName  = String.format("%s/%s_%s.sql",  BACKUP_DIR, dbName, today);
//
//        File backupDir = new File(BACKUP_DIR);
//
//        try {
//            if (!backupDir.exists() && !backupDir.mkdirs()) {
//                logger.error("Kunde inte skapa backup-katalog: {}", BACKUP_DIR);
//                return;
//            }
//
//            // === Skapa binär backup (.dump) ===
//            createBackupFile(dumpFileName, host, dbName, "-Fc", "binär");
//
//            // === Skapa läsbar SQL-backup (.sql) ===
//            createBackupFile(sqlFileName, host, dbName, "-Fp", "SQL");
//
//        } catch (Exception e) {
//            logger.error("Fel under backup-processen: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Hjälpmetod för att skapa en backupfil i valt format
//     */
//    private void createBackupFile(String fileName, String host, String dbName, String formatFlag, String label)
//            throws IOException, InterruptedException {
//
//        ProcessBuilder pb = new ProcessBuilder(
//                PG_DUMP_PATH,
//                "-U", dbUser,
//                "-h", host,
//                formatFlag,
//                "-f", fileName,
//                dbName
//        );
//
//        pb.environment().put("PGPASSWORD", dbPassword);
//
//        logger.info("Startar {} backup av databasen...", label);
//
//        Process process = pb.start();
//        int exitCode = process.waitFor();
//
//        if (exitCode == 0) {
//            logger.info("{} backup skapad: {}", label, fileName);
//        } else {
//            logger.error("{} backup misslyckades (kod {}): {}", label, exitCode, fileName);
//        }
//    }
//
//    /**
//     * Hjälpfunktioner för att läsa ut host och databasnamn ur JDBC-URL:en
//     */
//    private String extractHost(String jdbcUrl) {
//        // Ex: jdbc:postgresql://localhost:5432/bolaneradar
//        String noPrefix = jdbcUrl.replace("jdbc:postgresql://", "");
//        return noPrefix.split(":")[0];
//    }
//
//    private String extractDbName(String jdbcUrl) {
//        // Ex: jdbc:postgresql://localhost:5432/bolaneradar
//        return jdbcUrl.substring(jdbcUrl.lastIndexOf("/") + 1);
//    }
//}