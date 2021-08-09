package rs.ac.bg.fon.mmklab.peer.service.util;

import java.nio.file.*;

public final class FileValidator {

    private FileValidator(){}

    public static boolean isValid(String pathString) {
        Path pathToConfigDir;
        try {
            pathToConfigDir = Paths.get(pathString);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }

        return exists(pathToConfigDir) && Files.isDirectory(pathToConfigDir, LinkOption.NOFOLLOW_LINKS) && checkPermissions(pathToConfigDir);
    }

    private static boolean checkPermissions(Path path) {
        return (Files.isReadable(path) && Files.isWritable(path));
    }

    private static boolean exists(Path path) {
        return (Files.exists(path, LinkOption.NOFOLLOW_LINKS));
    }
}
