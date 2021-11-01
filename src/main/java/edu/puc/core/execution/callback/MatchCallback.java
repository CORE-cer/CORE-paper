package edu.puc.core.execution.callback;

import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.execution.structures.output.ComplexEvent;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.profiling.Profiler;
import edu.puc.core.util.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Consumer;

import static edu.puc.core.engine.BaseEngine.jvmId;
import static edu.puc.core.execution.callback.MatchCallbackType.PRINT;

public class MatchCallback implements Consumer<CDSComplexEventGrouping> {
    private final String outputBaseDir = System.getProperty("user.dir") + "/files/output/" + jvmId + "_";
    private final Object[] args;
    private final MatchCallbackType type;
    public static int limit = 10;
    private int count = 0;

    public static MatchCallback getDefault() {
        // Default callback is PRINT
        return new MatchCallback(PRINT);
    }

    public MatchCallback(MatchCallbackType type, Object... args) {
        checkArgs(type, args);
        this.type = type;
        this.args = args;
    }

    private void checkArgs(MatchCallbackType type, Object... args) {
        try {
            switch (type) {
                case PRINT:
                    // Receives no args
                    break;
                case WRITE:
                    if (args.length != 0)  {
                        String name = (String) args[0];
                        if (name.length() == 0) throw new Exception("Empty file base name. " +
                                "Must provide a proper name or no name at all");
                        if (name.equals("all")) throw new Exception("Name 'all' is reserved.");
                    }
                    break;
                case EMAIL:
                    if (args.length == 0) throw new Exception("Must provide email address");
                    String address = (String) args[0];
                    if (! EmailValidator.getInstance().isValid(address)) throw new Exception("Invalid email address");
                    break;
                case NOTHING:
                    break;
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid argument(s) for MatchCallback of type " + type +
                    "\nArgument(s) provided:" + Arrays.toString(args) +
                    "\nArgument error cause:" + e.getMessage());
        }
    }

    private void printCallback(CDSComplexEventGrouping matches) {
        count = 0;
        System.err.println("Event " + matches.getLastEvent() + " triggered matches:");
//        Profiler.incrementMatches();

        for (ComplexEvent match: matches) {
            if (count >= limit) {
                break;
            }
            if (match != null) {
                Profiler.incrementMatches();
                System.err.print("\t");
                System.err.print("In interval [" + match.getStart() + ", " + match.getEnd() + "]: ");
                count++;
            }
        }
        System.err.println();
    }

    private void writeCallback(CDSComplexEventGrouping matches) {
        try {
            int event_count = 0;
            String baseName = args.length == 0 ? "all" : (String) args[0];
            BufferedWriter writer = StringUtils.getWriter(outputBaseDir + baseName + "_matches.txt", true);
            StringBuilder out = new StringBuilder(
                    ("Event " + matches.getLastEvent() + " triggered " + matches.size() + " different outputs:\n"));

            for (ComplexEvent complexEvent : matches) {
                out.append("\t");
                for (Event event : complexEvent) {
                    out.append(event).append(" ");
                    if (event_count++ == 100) {
                        writer.write(String.valueOf(out));
                        out.setLength(0); // Flush
                        event_count = 0;
                    }
                }
                out.append("\n");
            }
            out.append("\n");

            writer.write(String.valueOf(out));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void emailCallback(CDSComplexEventGrouping matches) {
        try {
            String address = (String) args[0];

            /* email here */
            String from = "";
            String host = "";

            //Get the session object
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    
                    /* password here */
                    return new PasswordAuthentication(from, "");
                }
            });

            StringBuilder out = new StringBuilder(
                    ("Event " + matches.getLastEvent() + " triggered " + matches.size() + " different outputs:\n")
            );

            for (ComplexEvent complexEvent : matches) {
                out.append("\t");
                for (Event event : complexEvent) {
                    out.append(event).append(" ");
                }
                out.append("\n");
            }
            out.append("\n");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(address));
            message.setSubject("CORE ha encontrado un nuevo match");
            message.setText(out.toString());

            // Send message
            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void accept(CDSComplexEventGrouping matches) {
        switch (type) {
            case PRINT:
                printCallback(matches);
                break;
            case WRITE:
                writeCallback(matches);
                break;
            case EMAIL:
                emailCallback(matches);
                break;
            case NOTHING:
                break;
        }
    }
}
