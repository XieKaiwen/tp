import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.*;

public class Alpha139Agent {
    static Path home;
    static Stage stage;
    static String phase;
    static List<String> log = new ArrayList<>();
    public static void premain(String args, Instrumentation inst) {
        phase = args;
        home = Path.of(System.getProperty("alpha.home"));
        AtomicBoolean started = new AtomicBoolean();
        Thread watcher = new Thread(() -> {
            for (int i=0;i<600 && !started.get();i++) {
                try {
                    Thread.sleep(100);
                    Platform.runLater(() -> {
                        if (started.get()) return;
                        Stage found = Window.getWindows().stream().filter(w -> w.isShowing()
                            && w.getScene()!=null && w.getScene().lookup("#commandTextField")!=null)
                            .map(w -> (Stage)w).findFirst().orElse(null);
                        if(found==null || !started.compareAndSet(false,true)) return;
                        stage=found;
                        try { run(); log.add("PASS phase "+phase); }
                        catch(Throwable e) { log.add("FAIL "+e); e.printStackTrace(); }
                        finally {
                            try { Files.write(home.resolve(phase+"-transcript.txt"),log); }
                            catch(Exception e) { e.printStackTrace(); }
                            submit("exit");
                        }
                    });
                } catch(IllegalStateException e) {} catch(InterruptedException e) { return; }
            }
        },"alpha139-ui-automation"); watcher.setDaemon(true); watcher.start();
    }
    static TextField field(){return (TextField)stage.getScene().lookup("#commandTextField");}
    static String feedback(){return ((TextArea)stage.getScene().lookup("#resultDisplay")).getText();}
    static void submit(String command) {
        field().setText(command); field().fireEvent(new ActionEvent());
        log.add("COMMAND "+command+"\nACTUAL "+feedback());
        log.add("FOCUS "+stage.getScene().getFocusOwner());
    }
    static void expect(String text) { if(!feedback().contains(text)) throw new AssertionError(text+" != "+feedback()); }
    static void unchanged(byte[] before) throws Exception {
        if(!Arrays.equals(before,Files.readAllBytes(home.resolve("data/addressbook.json"))))
            throw new AssertionError("roster bytes changed");
        log.add("PASS roster bytes unchanged");
    }
    static void shot(String name) throws Exception {
        stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
        WritableImage im=stage.getScene().snapshot(null); BufferedImage out=new BufferedImage((int)im.getWidth(),(int)im.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<out.getHeight();y++) for(int x=0;x<out.getWidth();x++) out.setRGB(x,y,im.getPixelReader().getArgb(x,y));
        ImageIO.write(out,"png",home.resolve(name+".png").toFile());
    }
    static void run() throws Exception {
        log.add("METHOD Java agent UI automation; not personal/manual keyboard evidence");
        log.add("OS "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch"));
        log.add("JAVA "+System.getProperty("java.runtime.version"));
        for(Screen screen:Screen.getScreens()) log.add("SCREEN bounds="+screen.getBounds()+" visual="+screen.getVisualBounds()+" outputScale="+screen.getOutputScaleX()+","+screen.getOutputScaleY());
        stage.setWidth(740); stage.setHeight(600);
        Path roster=home.resolve("data/addressbook.json");
        if(phase.equals("seed")) {
            if(Files.exists(roster)) throw new AssertionError("not empty startup");
            shot("01-empty");
            submit("student add /name Alpha Tester /email E9390001@U.NUS.EDU /telegram @alpha_tester /github alpha-tester"); expect("Added student: Alpha Tester.");
            byte[] saved=Files.readAllBytes(roster);
            submit("student add /name Other Name /email e9390001@u.nus.edu"); expect("already exists"); unchanged(saved); shot("02-duplicate");
            submit("student add /name Missing Email"); unchanged(saved);
            submit("student add /name Invalid /email bad@example.com"); unchanged(saved);
            submit("student add /name "+"N".repeat(100)+" /email e9390002@u.nus.edu"); expect("Added student:"); shot("03-long-profile");
            submit("edit /email e9390001@u.nus.edu /telegram @updated_alpha /github updated-alpha"); expect("Updated student:");
            submit("view e9390001@u.nus.edu"); shot("04-contact-edit");
            saved=Files.readAllBytes(roster);
            submit("delete e9390001@u.nus.edu"); unchanged(saved); shot("05-deletion-preview");
            submit("find Alpha"); expect("Pending deletion cancelled."); unchanged(saved);
            submit("confirm-delete e9390001@u.nus.edu"); expect("There is no pending deletion"); unchanged(saved);
            submit("delete e9390001@u.nus.edu");
            submit("confirm-delete e9390001@u.nus.edu"); expect("Deleted student:");
            submit("view e9390002@u.nus.edu"); shot("06-survivor");
        } else if(phase.equals("restart")) {
            String json=Files.readString(roster);
            if(!json.contains("e9390002@u.nus.edu") || json.contains("e9390001@u.nus.edu")) throw new AssertionError("restart data wrong");
            submit("view e9390002@u.nus.edu"); shot("07-restarted");
        } else if(phase.equals("failure")) {
            byte[] saved=Files.readAllBytes(roster);
            submit("view e9390002@u.nus.edu");
            submit("student add /name Save Failure /email e9390003@u.nus.edu"); expect("could not be saved"); unchanged(saved); shot("08-failed-save");
            if(Files.readString(roster).contains("e9390003@u.nus.edu")) throw new AssertionError("failed record persisted");
        } else if(phase.equals("recovery")) {
            byte[] saved=Files.readAllBytes(roster);
            expect("Stored data could not be loaded"); shot("09-readonly-recovery");
            submit("student add /name Recovery /email e9390004@u.nus.edu"); expect("Data changes are disabled"); unchanged(saved);
            submit("list"); unchanged(saved); shot("10-recovery-list");
        } else throw new AssertionError("unknown phase");
    }
}
