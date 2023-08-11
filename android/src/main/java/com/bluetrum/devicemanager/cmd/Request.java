package com.bluetrum.devicemanager.cmd;

public abstract class Request implements Command {

    private final byte command;
    private final byte commandType;

    private static final int DEFAULT_REQUEST_TIMEOUT = 10000;

    private int timeout = DEFAULT_REQUEST_TIMEOUT;

    public Request(byte command) {
        this.command = command;
        this.commandType = COMMAND_TYPE_REQUEST;
    }

    @Override
    public byte getCommand() {
        return command;
    }

    @Override
    public byte getCommandType() {
        return commandType;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean withResponse() {
        return true;
    }

}
