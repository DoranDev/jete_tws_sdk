package com.bluetrum.devicemanager.cmd;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Response implements Command {

    private final byte command;
    private final byte commandType;

    private final byte[] commandData;

    public Response(byte command) {
        this(command, null);
    }

    public Response(byte command, byte[] commandData) {
        this.command = command;
        this.commandType = COMMAND_TYPE_RESPONSE;
        this.commandData = commandData;
    }

    @Override
    public byte getCommand() {
        return command;
    }

    @Override
    public byte getCommandType() {
        return commandType;
    }

    @Override
    public byte[] getPayload() {
        return commandData;
    }

    @NonNull
    @Override
    public String toString() {
        return "Response{" +
                "command=" + command +
                ", commandType=" + commandType +
                ", commandData=" + Arrays.toString(commandData) +
                '}';
    }
}
