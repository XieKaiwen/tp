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

public class Review140Agent {
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
        } else if(phase.equals("review140")) {
            if(Files.exists(roster)) throw new AssertionError("requires clean folder");
            submit("student add /name Zoe Lee /email e9480001@u.nus.edu");
            submit("student add /name Émile Ng /email e9480002@u.nus.edu");
            submit("student add /name Eve Ong /email e9480003@u.nus.edu");
            submit("find e948");
            ListView<?> rows=(ListView<?>)stage.getScene().lookup("#personListView");
            String order=rows.getItems().stream().map(p -> ((seedu.address.model.person.Person)p).getName().fullName).toList().toString();
            log.add("OBSERVED #167 name order "+order);
            if(!order.equals("[Eve Ong, Zoe Lee, Émile Ng]")) throw new AssertionError("ordering changed");
            shot("01-accent-order");
            submit("student add /name Alex\u00a0Tan /email e9480004@u.nus.edu");
            submit("student add /name Alex Tan /email e9480005@u.nus.edu");
            submit("find Alex Tan"); expect("1 student found");
            shot("02-nbsp-search");
            submit("student add /name E\u0301mile Ng /email e9480006@u.nus.edu");
            submit("find Émile"); expect("1 student found");
            log.add("OBSERVED #168 composed query result "+((seedu.address.model.person.Person)((ListView<?>)stage.getScene().lookup("#personListView")).getItems().get(0)).getEmail().value);
            submit("find E\u0301mile"); expect("1 student found");
            log.add("OBSERVED #168 decomposed query result "+((seedu.address.model.person.Person)((ListView<?>)stage.getScene().lookup("#personListView")).getItems().get(0)).getEmail().value);
            shot("03-nfd-search");
            submit("enrol /email e9480005@u.nus.edu /module CS2103T /semester AY26/27 S1 /section Not assigned /team Not assigned"); expect("Added enrolment");
            if(Files.readString(roster).split("Not assigned",-1).length-1!=2) throw new AssertionError("literal placeholders not stored");
            shot("04-literal-placeholder");
            submit("edit-enrol /email e9480005@u.nus.edu /module CS2103T /semester AY26/27 S1 /section clear"); expect("Updated enrolment");
            if(Files.readString(roster).split("Not assigned",-1).length-1!=1) throw new AssertionError("clear failed");
            shot("05-cleared-placeholder");
            submit("edit-enrol /email e9480005@u.nus.edu /module CS2103T /semester AY26/27 S1 /section T14 /team clear");
            if(!feedback().equals("Updated enrolment for Alex Tan: CS2103T, AY26/27 S1.")) throw new AssertionError("#172 feedback changed");
            shot("06-edit-enrol-feedback");
            byte[] saved=Files.readAllBytes(roster);
            submit("student add /name Too Long /email "+"a".repeat(65)+"@u.nus.edu"); expect("Email must"); unchanged(saved);
            boolean lengthShown=feedback().contains("1 to 64"); if(lengthShown!=Boolean.getBoolean("review.v15")) throw new AssertionError("unexpected email length guidance");
            shot("07-overlong-email");
            submit("edit /email e9480005@u.nus.edu /new-email "+"a".repeat(65)+"@u.nus.edu"); expect("New email:"); unchanged(saved);
            if(feedback().contains("1 to 64")!=Boolean.getBoolean("review.v15")) throw new AssertionError("new email guidance");
            submit("student add /name Boundary /email "+"a".repeat(64)+"@u.nus.edu"); expect("Added student: Boundary.");
            submit("enrol /email e9480005@u.nus.edu /module CS2101 /semester AY26/27 S1 /section Not assigned"); expect("Added enrolment");
            submit("view e9480005@u.nus.edu"); shot("08-profile-enrolments");
        } else if(phase.equals("review140-restart")) {
            String json=Files.readString(roster);
            if(!json.contains("Not assigned") || !json.contains("Alex\u00a0Tan") || !json.contains("E\u0301mile Ng")) throw new AssertionError("literal/Unicode restart data changed");
            submit("view e9480005@u.nus.edu"); expect("Viewing student: Alex Tan.");
            submit("find Alex Tan"); expect("1 student found");
            shot("09-reloaded");
        } else if(phase.equals("explore")) {
            if(Files.exists(roster)) throw new AssertionError("exploration refuses an existing roster");
            submit("student add /name Boundary Alpha /email E9380001@U.NUS.EDU"); expect("Added student:");
            byte[] saved=Files.readAllBytes(roster);
            String[] invalid = {
                "student add /name /email e9380002@u.nus.edu",
                "student add /name "+"n".repeat(101)+" /email e9380002@u.nus.edu",
                "student add /name A/B /email e9380002@u.nus.edu",
                "student add /name Valid /email "+"a".repeat(65)+"@u.nus.edu",
                "student add /name Valid /email a..b@u.nus.edu",
                "student add /name Valid /email -alpha@u.nus.edu",
                "student add /name Valid /email alpha.@u.nus.edu",
                "student add /name Valid /email alpha@example.com",
                "student add /name Valid /email e9380002@u.nus.edu /telegram abcd",
                "student add /name Valid /email e9380002@u.nus.edu /telegram "+"a".repeat(33),
                "student add /name Valid /email e9380002@u.nus.edu /telegram 1abcd",
                "student add /name Valid /email e9380002@u.nus.edu /telegram @@abcde",
                "student add /name Valid /email e9380002@u.nus.edu /telegram",
                "student add /name Valid /email e9380002@u.nus.edu /github "+"a".repeat(40),
                "student add /name Valid /email e9380002@u.nus.edu /github -alpha",
                "student add /name Valid /email e9380002@u.nus.edu /github alpha-",
                "student add /name Valid /email e9380002@u.nus.edu /github alpha--beta",
                "student add /name Valid /email e9380002@u.nus.edu /github @alpha",
                "student add /name Valid /email e9380002@u.nus.edu /github alpha_beta",
                "student add /name Valid /email e9380002@u.nus.edu /github",
                "student add /name Valid /email e9380002@u.nus.edu /email e9380003@u.nus.edu",
                "student add /name Valid /email e9380002@u.nus.edu /unknown value",
                "student add unexpected /name Valid /email e9380002@u.nus.edu",
                "edit /email e9380001@u.nus.edu /github alpha--beta",
                "edit /email e9380001@u.nus.edu /telegram abcd",
                "edit /email e9380001@u.nus.edu /new-email bad /name Changed",
                "edit /email e9380001@u.nus.edu /name A/B /github changed",
                "edit /email e9380001@u.nus.edu /name",
                "edit /email e9380001@u.nus.edu",
                "view", "view bad", "view e9380001@u.nus.edu extra",
                "delete", "delete bad", "delete e9380001@u.nus.edu extra"
            };
            for(String cmd:invalid) {
                submit(cmd);
                if(!field().getStyleClass().contains("error")) throw new AssertionError("invalid command accepted: "+cmd);
                unchanged(saved);
            }
            shot("11-boundary-errors");
            submit("edit /email e9380001@u.nus.edu /telegram "+"a".repeat(32)+" /github "+"g".repeat(39)); expect("Updated student:");
            submit("edit /email e9380001@u.nus.edu /telegram clear /github clear"); expect("Updated student:");
            String json=Files.readString(roster);
            if(json.contains("a".repeat(32))||json.contains("g".repeat(39))) throw new AssertionError("contacts not cleared");
            submit("edit /email e9380001@u.nus.edu /telegram @clear /github Clear"); expect("Updated student:");
            json=Files.readString(roster);
            if(!json.contains("clear")||!json.contains("Clear")) throw new AssertionError("literal clear lost");
            saved=Files.readAllBytes(roster);
            submit("edit /email E9380001@U.NUS.EDU /new-email E9380001@U.NUS.EDU"); expect("No values changed."); unchanged(saved);
            submit("student add /name Boundary Beta /email e9380002@u.nus.edu"); expect("Added student:");
            saved=Files.readAllBytes(roster);
            submit("edit /email e9380001@u.nus.edu /new-email E9380002@U.NUS.EDU /name Changed"); expect("already exists"); unchanged(saved);
            submit("edit /email e9380001@u.nus.edu /new-email e9380011@u.nus.edu /name   Boundary   Renamed"); expect("Updated student: Boundary Renamed.");
            saved=Files.readAllBytes(roster);
            submit("view e9380001@u.nus.edu"); expect("No student found"); unchanged(saved);
            submit("view e9380011@u.nus.edu"); expect("Viewing student: Boundary Renamed.");
            String[] cancel = {"unknowncommand", "", "view e9380002@u.nus.edu", "find Boundary"};
            for(String cmd:cancel) {
                submit("delete e9380011@u.nus.edu"); unchanged(saved);
                submit(cmd); expect("Pending deletion cancelled."); unchanged(saved);
                submit("confirm-delete e9380011@u.nus.edu"); expect("There is no pending deletion"); unchanged(saved);
            }
            submit("delete e9380011@u.nus.edu");
            submit("confirm-delete e9380002@u.nus.edu"); expect("Deletion was cancelled"); unchanged(saved);
            submit("confirm-delete e9380011@u.nus.edu"); expect("There is no pending deletion"); unchanged(saved);
            submit("find Boundary"); shot("12-explored-profiles");
            submit("student add /name One /email "+"a".repeat(64)+"@u.nus.edu /telegram abcde /github a"); expect("Added student: One.");
            submit("view e9380011@u.nus.edu"); shot("13-literal-clear-profile");
        } else if(phase.equals("explore-restart")) {
            submit("view e9380011@u.nus.edu"); expect("Viewing student: Boundary Renamed.");
            String json=Files.readString(roster);
            if(json.contains("e9380001@u.nus.edu") || !json.contains("e9380011@u.nus.edu") || !json.contains("Clear")) throw new AssertionError("identity/contact reload wrong");
            submit("view e9380002@u.nus.edu"); expect("Viewing student: Boundary Beta.");
            shot("14-exploration-restart");
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
