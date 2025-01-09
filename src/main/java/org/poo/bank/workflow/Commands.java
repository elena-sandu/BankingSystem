package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface Commands {
    void execute(ArrayNode output);
}
