package es.upm.babel.sequenceTester;

/**
 * A wrapper process that tries to monitor if the execution of the
 * abstract method toTry is blocked or not.
 */
abstract class Tryer extends Thread {
   volatile private boolean started = false;
   volatile private boolean blocked = true;
   volatile private boolean raisedException = false;
   volatile private Throwable throwable = null; 

   boolean isBlocked() {
      if (!started) gimmeTime(0);
      return blocked;
   }

   boolean raisedException() {
     if (!started) {
       gimmeTime(0);
     }
      return raisedException;
   }

   Throwable getException() {
       return throwable;
   }

   // Un sleep sin excepciones
   static void sleep (int ms) {
      long initialTimeMillis = System.currentTimeMillis();
      long remainTimeMillis = ms;
      
      while (remainTimeMillis > 0) {
         try { Thread.sleep(remainTimeMillis); }
         catch (InterruptedException e) { }
         remainTimeMillis = ms - (System.currentTimeMillis() - initialTimeMillis);
      }
   }

   void gimmeTime(int ms) {
      while (!started) { }
      Tryer.sleep(ms);
   }

   public void run() {
      blocked = true;
      started = true;
      try { this.toTry(); blocked = false; }
      catch (Throwable t) { raisedException = true; throwable = t; }
   }

   abstract void toTry() throws Throwable;
}
