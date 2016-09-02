package execPy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecPy {

    public static void run(String docFileName) throws IOException, InterruptedException {

        String classPath = String.valueOf(ExecPy.class.getResource(""));
        classPath = classPath.substring(classPath.indexOf("/") + 1);
        System.out.println("classPath : " + classPath.substring(classPath.indexOf("/") + 1));
//        String PyDirPath = "C:/Users/Billy/Documents/GitHub/OfficialProgram/Paper/web/";
        String checkPy = /*PyDirPath + */classPath + "check.py";
        String modifyPy = /*PyDirPath + */classPath + "modify.py";
        String DataDirPath = "C:/Users/Billy/Documents/GitHub/OfficialProgram/Paper/data/" + docFileName + "/";

        try {

            System.out.println("check start");
            Process checkProcess = Runtime.getRuntime().exec("python" + " " + checkPy + " " + classPath/*PyDirPath*/ + " " + DataDirPath);
            String line;

            BufferedReader inputStream = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            while ((line = inputStream.readLine()) != null) {
                System.out.println(line);
            }
            inputStream.close();

            BufferedReader errorStream = new BufferedReader(new InputStreamReader(checkProcess.getErrorStream()));
            while ((line = errorStream.readLine()) != null) {
                System.out.println(line);
            }
            errorStream.close();

            checkProcess.waitFor();//等待check程序执行完毕
            System.out.println("check end");

            System.out.println("modify start");
            Process modifyProcess = Runtime.getRuntime().exec("python" + " " + modifyPy + " " + DataDirPath);

            inputStream = new BufferedReader(new InputStreamReader(modifyProcess.getInputStream()));
            while ((line = inputStream.readLine()) != null) {
                System.out.println(line);
            }
            inputStream.close();

            errorStream = new BufferedReader(new InputStreamReader(modifyProcess.getErrorStream()));
            while ((line = errorStream.readLine()) != null) {
                System.out.println(line);
            }
            errorStream.close();

            modifyProcess.waitFor();//等待modify程序执行完毕
            System.out.println("modify end");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String classPath = String.valueOf(ExecPy.class.getResource(""));
        System.out.println("classPath : " + classPath.substring(classPath.indexOf("/") + 1));
    }
}
