package jSci;

public class Main {
  public static void main(String[] args) {
    boolean eschile = false;
    boolean escol = false;
    boolean esarg = true;
    if (args.length == 1) {
      if (args[0].equals("cl")) {
        eschile = true;
        esarg = false;
      } 
      if (args[0].equals("co")) {
        escol = true;
        esarg = false;
      } 
    } 
    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    Distribuidor distribuidor = new Distribuidor(esarg, eschile, escol);
  }
}
