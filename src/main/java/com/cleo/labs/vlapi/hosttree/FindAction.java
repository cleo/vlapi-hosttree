package com.cleo.labs.vlapi.hosttree;

import java.io.File;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.cleo.lexicom.external.ILexiCom;
import com.cleo.lexicom.external.LexiComFactory;

public class FindAction {
    private static ILexiCom lexicom = null;

    public static void connect() throws Exception {
        lexicom = LexiComFactory.getVersaLex(LexiComFactory.HARMONY, new File(".").getAbsolutePath(), LexiComFactory.CLIENT_ONLY);
    }

    public static void disconnect() {
        try {
            if (lexicom != null) {
                lexicom.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrintOnceHeader {
        private String header;
        private boolean printed;
        public PrintOnceHeader(String header) {
            this.header = header;
            this.printed = false;
        }
        public void print() {
            if (!printed) {
                System.out.println(header);
                printed = true;
            }
        }
    }

    private static boolean anyMatch(String commands, Pattern[] patterns) {
        if (patterns.length==0) {
            return true;
        } else {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(commands).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printCommands(String commands, String indent, PrintOnceHeader...headers) {
        Stream.of(headers).forEach(PrintOnceHeader::print);
        Stream.of(commands.split("\n")).forEach(c -> System.out.println(indent+c));
    }

    public static void main(String[] argv) {
        try {
            Pattern[] patterns = Stream.of(argv)
                    .map(p -> Pattern.compile(p, Pattern.DOTALL))
                    .toArray(Pattern[]::new);
            connect();
            for (String host : lexicom.list(ILexiCom.HOST, new String[0])) {
                PrintOnceHeader hostHeader = new PrintOnceHeader("host "+host);
                for (String mailbox : lexicom.list(ILexiCom.MAILBOX, new String[] {host})) {
                    PrintOnceHeader mailboxHeader = new PrintOnceHeader(". mailbox "+mailbox);
                    for (String action : lexicom.list(ILexiCom.ACTION, new String[] {host, mailbox})) {
                        PrintOnceHeader actionHeader = new PrintOnceHeader(". . action "+action);
                        String commands = lexicom.getProperty(ILexiCom.ACTION, new String[] {host, mailbox, action}, "Commands")[0];
                        if (anyMatch(commands, patterns)) {
                            printCommands(commands, ". . | ", hostHeader, mailboxHeader, actionHeader);
                        }
                    }
                }
                for (String hostaction : lexicom.list(ILexiCom.HOST_ACTION, new String[] {host})) {
                    PrintOnceHeader hostactionHeader = new PrintOnceHeader(". hostaction "+hostaction);
                    String commands = lexicom.getProperty(ILexiCom.HOST_ACTION, new String[] {host, hostaction}, "Commands")[0];
                    if (anyMatch(commands, patterns)) {
                        printCommands(commands, ". | ", hostHeader, hostactionHeader);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        System.exit(0);
    }
}
