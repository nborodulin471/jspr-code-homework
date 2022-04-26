package httpServer.dao;

import httpServer.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @implNote Здесь "типо" работаем с данными. Класс слоя Dao должны реализовывать работу с БД,
 * но в учебных целях "пока" работаем с получением любых данных
 */
public abstract class Dao {

    public static void getFile(Server server, String path, BufferedOutputStream out){
        final String mimeType;
        try {
            final var filePath = Path.of(".", "public", path);
            mimeType = Files.probeContentType(filePath);
            server.answerOk(out, mimeType, Files.readAllBytes(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
