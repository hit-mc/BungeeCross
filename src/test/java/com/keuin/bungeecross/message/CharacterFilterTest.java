package com.keuin.bungeecross.message;

import com.keuin.bungeecross.util.CharacterFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CharacterFilter Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>11月 9, 2020</pre>
 */
public class CharacterFilterTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: containsInvalidCharacter(String string)
     */
    @Test
    public void testContainsInvalidCharacter() throws Exception {
        // Invalid cases
        //// emoji
        assertTrue(CharacterFilter.containsInvalidCharacter("test😄me"));
        assertTrue(CharacterFilter.containsInvalidCharacter("test😈😁me"));
        //// special chars
        assertTrue(CharacterFilter.containsInvalidCharacter("test\nme"));
        assertTrue(CharacterFilter.containsInvalidCharacter("\n"));
        assertTrue(CharacterFilter.containsInvalidCharacter("\b"));
        assertTrue(CharacterFilter.containsInvalidCharacter("\t"));

        // Valid cases
        assertFalse(CharacterFilter.containsInvalidCharacter(""));
        //// commands
        assertFalse(CharacterFilter.containsInvalidCharacter("history"));
        assertFalse(CharacterFilter.containsInvalidCharacter("help"));
        assertFalse(CharacterFilter.containsInvalidCharacter("list"));
        assertFalse(CharacterFilter.containsInvalidCharacter("reload"));
        //// chinese
        assertFalse(CharacterFilter.containsInvalidCharacter("这合理吗"));
        //// japanese
        assertFalse(CharacterFilter.containsInvalidCharacter("あいうえおアイウエオ"));
        //// valid special chars
        assertFalse(CharacterFilter.containsInvalidCharacter("list-all"));
        assertFalse(CharacterFilter.containsInvalidCharacter("new_file"));
        assertFalse(CharacterFilter.containsInvalidCharacter("a/b"));
        //// math expressions
        assertFalse(CharacterFilter.containsInvalidCharacter("1+5"));
        assertFalse(CharacterFilter.containsInvalidCharacter("3-4*(8-2.71828)"));
        assertFalse(CharacterFilter.containsInvalidCharacter("(7^2-4*9*2)/(2*9)"));
        assertFalse(CharacterFilter.containsInvalidCharacter("{4+5*[9-2/(1+5)]}/3.14"));
    }


} 
