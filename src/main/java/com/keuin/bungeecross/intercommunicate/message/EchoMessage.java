package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.MessageUserFactory;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class EchoMessage extends Message {

    private final String echo;
    private final BaseComponent[] baseComponents;
    private final String instruction;

    public EchoMessage(String instruction, String echo) {
        if (instruction == null)
            throw new IllegalArgumentException("instruction is null");
        if (echo == null)
            throw new IllegalArgumentException("echo message is null");
        this.echo = echo;
        this.instruction = instruction;
        this.baseComponents = new ComponentBuilder(echo).create();
    }

    public EchoMessage(String instruction, BaseComponent[] baseComponents) {
        BaseComponent[] copy = new BaseComponent[baseComponents.length];
        System.arraycopy(baseComponents, 0, copy, 0, copy.length);
        this.baseComponents = copy;
        this.instruction = instruction;
        this.echo = MessageUtil.getPlainTextOfBaseComponents(baseComponents);
    }

    public EchoMessage(String instruction, BaseComponent baseComponent) {
        this(instruction, new BaseComponent[]{baseComponent});
    }

    public String getInstruction() {
        return instruction;
    }

    @Override
    public String getMessage() {
        return echo;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        BaseComponent[] copy = new BaseComponent[baseComponents.length];
        System.arraycopy(baseComponents, 0, copy, 0, copy.length);
//        Logger.getLogger("getRichTextMessage").info(Arrays.toString(copy));
        return copy;
    }

    @Override
    public MessageUser getSender() {
        return MessageUserFactory.getConsoleUser(instruction);
    }

    @Override
    public boolean isJoinable() {
        return true;
    }
}
