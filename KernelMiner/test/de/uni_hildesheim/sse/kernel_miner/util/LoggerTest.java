package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class LoggerTest {

    @Test
    public void testLevels() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        l.logError("a");
        l.logWarning("a");
        l.logInfo("a");
        l.logException("a", new Exception());
        
        String[] lines = out.toString().split("\n");
        
        Assert.assertTrue(lines[0].startsWith("[error]"));
        Assert.assertTrue(lines[1].startsWith("[warning]"));
        Assert.assertTrue(lines[2].startsWith("[info]"));
        Assert.assertTrue(lines[3].startsWith("[error]"));
    }
    
    @Test
    public void testContent() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        String content = "This is a test text";
        l.logInfo(content);
        
        String result = out.toString();
        
        Assert.assertTrue(result.endsWith(content + "\n"));
    }
    
    @Test
    public void testMultipleLines() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        String line0 = "This is a test text";
        String line1 = "This is another test text";
        String line2 = "This is another line in the output";
        l.logInfo(line0, line1, line2);
        
        String[] lines = out.toString().split("\n");
        
        Assert.assertTrue(lines[0].endsWith(line0));
        Assert.assertTrue(lines[1].endsWith(line1));
        Assert.assertTrue(lines[2].endsWith(line2));
    }
    
    @Test
    public void testLogException() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        try {
            throw new RuntimeException("This is a test");
        } catch (RuntimeException e) {
            l.logException("This is the comment", e);
        }
        
        String[] lines = out.toString().split("\n");
        Assert.assertTrue(lines[0].endsWith("This is the comment:"));
        Assert.assertTrue(lines[1].trim().equals("java.lang.RuntimeException: This is a test"));
        for (int i = 2; i < lines.length; i++) {
            Assert.assertTrue(lines[i].trim().startsWith("at "));
        }
    }
    
    @Test
    public void testLogExceptionWithcause() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        try {
            try {
                throw new RuntimeException("This is a test");
            } catch (RuntimeException e) {
                throw new RuntimeException("This is another test", e);
            }
        } catch (RuntimeException e) {
            l.logException("This is the comment", e);
        }
        
        String[] lines = out.toString().split("\n");
        Assert.assertTrue(lines[0].endsWith("This is the comment:"));
        Assert.assertTrue(lines[1].trim().equals("java.lang.RuntimeException: This is another test"));
        
        int i = 2;
        while (lines[i].trim().startsWith("at ")) {
            i++;
        }
        
        Assert.assertTrue(lines[i].trim().equals("Caused by:"));
        i++;
        Assert.assertTrue(lines[i].trim().equals("java.lang.RuntimeException: This is a test"));
        i++;
        for (; i < lines.length; i++) {
            Assert.assertTrue(lines[i].trim().startsWith("at "));
        }
    }
    
    @Test
    public void testThreadNames() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        Thread.currentThread().setName("test1");
        l.logInfo("test");
        
        Thread.currentThread().setName("test2");
        l.logInfo("test");
        
        String[] lines = out.toString().split("\n");
        
        Assert.assertTrue(lines[0].contains("[test1]"));
        Assert.assertFalse(lines[0].contains("[test2]"));
        
        Assert.assertTrue(lines[1].contains("[test2]"));
        Assert.assertFalse(lines[1].contains("[test1]"));
    }
    
    @Test
    public void testMultiThreading() throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        
        Worker w1 = new Worker("worker 1", "message 1");
        Worker w2 = new Worker("worker 2", "message 2");
        Worker w3 = new Worker("worker 3", "message 3");
        Worker w4 = new Worker("worker 4", "message 4");
        
        new Thread(w1).start();
        new Thread(w2).start();
        new Thread(w3).start();
        new Thread(w4).start();
        
        while (!w1.isDone() || !w2.isDone() || !w3.isDone() || !w4.isDone()) {
            Thread.sleep(10);
        }
        
        String[] lines = out.toString().split("\n");
        Assert.assertEquals(4 * 1000, lines.length);

        for (int i = 0; i < lines.length; i++) {
            Assert.assertTrue(lines[i].startsWith("[info]"));
            int number = Integer.parseInt(lines[i].substring(lines[i].length() - 1));
            Assert.assertTrue(lines[i].endsWith("[worker " + number + "] message " + number));
        }
    }
    
    private static class Worker implements Runnable {

        private String name;
        
        private String message;
        
        private boolean done;
        
        public Worker(String name, String message) {
            this.name = name;
            this.message = message;
            this.done = false;
        }
        
        public boolean isDone() {
            return done;
        }
        
        @Override
        public void run() {
            Thread.currentThread().setName(name);
            
            for (int i = 0; i < 1000; i++) {
                Logger.INSTANCE.logInfo(message);
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
            }
            
            done = true;
        }
        
    }
    
    @Test
    public void testTimestamp() throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.init(out);
        Logger l = Logger.INSTANCE; // just a shortcut
        
        l.logInfo("message 1");
        
        Thread.sleep(1000);
        
        l.logInfo("message 2");
        
        String[] lines = out.toString().split("\n");
        Assert.assertEquals(2, lines.length);
        
        Pattern p = Pattern.compile("\\[info\\] \\[(\\d+)\\].*");
        
        Matcher m = p.matcher(lines[0]);
        Assert.assertTrue(m.matches());
        int t1 = Integer.parseInt(m.group(1));
        
        m = p.matcher(lines[1]);
        Assert.assertTrue(m.matches());
        int t2 = Integer.parseInt(m.group(1));
        
        Assert.assertEquals(1, t2 - t1);
    }

}
