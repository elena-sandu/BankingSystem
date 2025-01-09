package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

import java.util.LinkedList;

public class Invoker {
    private LinkedList<Commands> commands = new LinkedList<>();
    public void newCommand(Commands command, ArrayNode output) {
        commands.push(command);
        command.execute(output);
    }
}
