import java.nio.file.*;
import java.util.logging.*;
import seedu.address.logic.*;
import seedu.address.model.*;
import seedu.address.storage.*;

public class Review136 {
  static int checks;
  static void check(boolean b, String msg) { if (!b) throw new AssertionError(msg); checks++; }
  public static void main(String[] args) throws Exception {
    Path home=Path.of("/private/tmp/socdex-136-review");
    ModelManager model=new ModelManager();
    LogicManager logic=new LogicManager(model,new StorageManager(new JsonAddressBookStorage(home.resolve("roster.json")),new JsonUserPrefsStorage(home.resolve("prefs.json"))));
    StringBuilder logs=new StringBuilder();
    Handler capture=new Handler(){public void publish(LogRecord r){logs.append(r.getMessage()).append('\n');}public void flush(){}public void close(){}};
    Logger.getLogger("ab3.LogicManager").addHandler(capture);
    logic.execute("student add /name Zulu Fiction /email e8999001@u.nus.edu");
    logic.execute("student add /name Alpha Fiction /email e8999002@u.nus.edu");
    check(logic.getFilteredPersonList().get(0).getName().fullName.equals("Alpha Fiction"),"initial sort");
    logic.execute("list");
    check(logic.getFilteredPersonList().get(0).getName().fullName.equals("Alpha Fiction"),"list preserves sort");
    logic.execute("remark 1 r/Private probe note");
    check(logic.getFilteredPersonList().get(0).getName().fullName.equals("Alpha Fiction"),"remark preserves sort");
    check(logic.getFilteredPersonList().get(0).getRemark().value.equals("Private probe note"),"display index target");
    check(logic.getFilteredPersonList().get(1).getRemark().value.isEmpty(),"other record unchanged");
    byte[] before=Files.readAllBytes(home.resolve("roster.json"));
    for(String bad:new String[]{"remark 1","list junk","help junk","clear junk","bye junk"}) {
      boolean rejected=false;
      try {logic.execute(bad);}catch(seedu.address.logic.parser.exceptions.ParseException e){rejected=true;}
      check(rejected,"reject "+bad);
      check(java.util.Arrays.equals(before,Files.readAllBytes(home.resolve("roster.json"))),"disk unchanged "+bad);
    }
    check(logic.getFilteredPersonList().get(0).getRemark().value.equals("Private probe note"),"remark remains");
    check(!logs.toString().contains("Alpha Fiction")&&!logs.toString().contains("Zulu Fiction")&&!logs.toString().contains("e899900")&&!logs.toString().contains("Private probe note")&&!logs.toString().contains("list junk"),"private values absent from lifecycle logger");
    check(logs.toString().contains("AddCommand")&&logs.toString().contains("RemarkCommand"),"operation markers retained");
    Files.writeString(home.resolve("captured-logic.log"),logs);
    System.out.println("PASS independent exact-JAR checks: "+checks);
  }
}
