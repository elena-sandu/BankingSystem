package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

import java.util.LinkedList;
/**
 * Implements the Command Design Pattern, allowing operations to be queued.
 */
public class Invoker {
    private LinkedList<Commands> commands = new LinkedList<>();
    /**
     * Adds a new command to the queue and executes it immediately.
     * @param command The command instance representing the operation to be executed.
     * @param output  The output to store the results or responses of the command execution.
     */
    public void newCommand(Commands command, ArrayNode output) {
        commands.push(command);
        command.execute(output);
    }
}
