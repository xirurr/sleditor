package Services;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

public class PathHelper {
    public static void appendToPath(String dir) {
        try {
            String path = System.getProperty("java.library.path");
            path = dir + ";" + path;
            System.setProperty("java.library.path", path);
            MethodHandles.Lookup cl = MethodHandles.privateLookupIn(ClassLoader.class, MethodHandles.lookup());
            VarHandle sys_paths = cl.findStaticVarHandle(ClassLoader.class, "sys_paths", String[].class);
            sys_paths.set(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}