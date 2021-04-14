package com.keuin.bungeecross.util;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageUtilTest {

    @Test
    public void testGetPlainTextOfBaseComponents() {
        testText("");
        testText("1");
        testText("\n");
        testText(" ");
        testText("\t");
        testText("[SYSTEM]\nTest tEXt\ncoMes -- HEre\t!!;)");
    }

    private void testText(String text) {
        var comp = new ComponentBuilder(text).create();
        var str = MessageUtil.getPlainTextOfBaseComponents(comp);
        assertEquals(text, str);
    }

}