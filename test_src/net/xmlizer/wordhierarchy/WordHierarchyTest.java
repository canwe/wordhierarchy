package net.xmlizer.wordhierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.xmlizer.permutation.PermutationHelper;

import org.junit.Test;

/**
 * Copyright (C) 2010 Bernhard Wagner
 * 
 * This file is part of wordhierarchy.
 * 
 * wordhierarchy is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

public class WordHierarchyTest {

	/*
	 * Ih -
	 * 	r
	 * 		e
	 * 			m
	 * 			n
	 * 			r
	 * 				seits
	 * 			s
	 * 				gleichen
	 * 			t -
	 * 				halber
	 * 				w -
	 * 					egen
	 * 					illen
	 * 		s
	 * 		ige
	 * 			m
	 * 			n
	 * 			s
	 * 			r
	 * 	nen
	 * 
	 */
	private static final String[] ihr = new String[] { "Ihnen", "Ihr", "Ihre",
			"Ihrem", "Ihren", "Ihrer", "Ihrerseits", "Ihres", "Ihresgleichen",
			"Ihrethalben", "Ihretwegen", "Ihretwillen", "Ihrige", "Ihrigem",
			"Ihrigen", "Ihriger", "Ihriges", "Ihrs", "Sie", };

	private static final String[] du = new String[] { "Dein", "Deine",
			"Deinem", "Deinen", "Deiner", "Deinerseits", "Deines",
			"Deinesgleichen", "Deinethalben", "Deinetwegen", "Deinetwillen",
			"Deinige", "Deinigem", "Deinigen", "Deiniger", "Deiniges", "Deins",
			"Dich", "Dir", "Du", "Euch", "Euer", "Euere", "Euerem", "Euerer",
			"Eueres", "Euers", "Euerseits", "Eure", "Eurem", "Euren",
			"Eurerseits", "Eures", "Euresgleichen", "Eurethalben",
			"Euretwegen", "Euretwillen", "Eurige", "Eurigem", "Eurigen",
			"Euriger", "Euriges", };

	private static final String[] shortEuch = "Euch Euer Eueres Euerem Eure Eurer"
			.split("\\s");

	@Test
	public void testEuer() {
		final Word tree = WordHierarchyBuilder.createWordTree(shortEuch);
		assertEquals("Eu(?:ch|er(?:e[ms])?|rer?)", toRegexSorted(tree));
	}

	@Test
	public void testEuerems() {
		final Word tree = WordHierarchyBuilder.createWordTree("Euerem Eueres"
				.split("\\s"));
		assertEquals("Euere[ms]", toRegexSorted(tree));
	}

	@Test
	public void testEueres() {
		final Word tree = WordHierarchyBuilder.createWordTree("Euere Eueres"
				.split("\\s"));
		assertEquals("Eueres?", toRegexSorted(tree));
	}

	// top level single chars not implemented yet, i.e. E U L will be E|U|L instead of [EUL]  
	// @Test
	public void testEUL() {
		final Word tree = WordHierarchyBuilder.createWordTree("E U L"
				.split("\\s"));
		assertEquals("[ELU]", toRegexSorted(tree));
	}
	
	// TODO:
	// 1. mix groups with character classes.
	// -------------------------------------
	// ab ac ad aef ag
	// all are children of a: b, c, d, ef, g
	// but not all have length 1, so we get:
	// a(?:g|b|d|ef|c)
	// instead of
	// a(?:ef[bcdg])
	// we should test for subgroups of children that have length 1
	// and put them together into a character class ("[...]") along inside
	// a group with the longer children, e.g. the children b,c,d,g as a subgroup
	// within the group with 1 sibling ef.
	// 2. top-level single characters should go into a character class, too.
	// -------------------------------------
	//
	// 3. we're recognizing common substrings from the left, but not from the right!
	// -------------------------------------
	// e.g. ab, ac, ad becomes a[bcd] but
	//      ba, ca, da becomes da|ca|ba (should become [dcb]a)
	//
	// 4. or the middle!
	// 
	// abcd ebcf should result in [ae]bc[df]
	// 
	
	// @Test
	public void testMixGroupsWithCharacterClasses() {
		final Word tree = WordHierarchyBuilder.createWordTree("ab ac ad aef ag"
				.split("\\s"));
		assertEquals("a(?:ef[bcdg])", toRegexSorted(tree));
	}
	
//	 @Test
	public void commonSubstringsFromRight() {
		final Word tree = WordHierarchyBuilder.createWordTree("ba ca da"
				.split("\\s"));
		assertEquals("[dcb]a", toRegexSorted(tree));
	}
	
//	 @Test
	public void commonSubstringsMiddle() {
		final Word tree = WordHierarchyBuilder.createWordTree("abcd ebcf"
				.split("\\s"));
		assertEquals("[ae]bc[df]", toRegexSorted(tree));
	}
	
//	@Test
	public void testEULe() {
		final Word tree = WordHierarchyBuilder.createWordTree("E U Le"
				.split("\\s"));
		assertEquals("E|Le|U", toRegexSorted(tree));
	}

	@Test
	public void testEULeL() {
		final Word tree = WordHierarchyBuilder.createWordTree("E U Le L"
				.split("\\s"));
		assertEquals("E|Le?|U", toRegexSorted(tree));
	}

	@Test
	public void testabcabd() {
		final Word tree = WordHierarchyBuilder.createWordTree("abc abd"
				.split("\\s"));
		assertEquals("ab[cd]", toRegexSorted(tree));
	}

	@Test
	public void testabcabdabe() {
		final Word tree = WordHierarchyBuilder.createWordTree("abc abd abe"
				.split("\\s"));
		assertEquals("ab[cde]", toRegexSorted(tree));
	}

	@Test
	public void testabcade() {
		final Word tree = WordHierarchyBuilder.createWordTree("abc ade"
				.split("\\s"));
		assertEquals("a(?:bc|de)", toRegexSorted(tree));
	}

	@Test
	public void testaabaac() {
		final Word tree = WordHierarchyBuilder.createWordTree("aab aac"
				.split("\\s"));
		assertEquals("aa[bc]", toRegexSorted(tree));
	}

	@Test
	public void testaaab() {
		final Word tree = WordHierarchyBuilder.createWordTree("aa ab"
				.split("\\s"));
		assertEquals("a[ab]", toRegexSorted(tree));
	}

	@Test
	public void testSameStringTwice() {
		final Word euchTree = WordHierarchyBuilder
				.createWordTree("SameStringTwice___SameStringTwice SameStringTwice___AndSome"
						.split("\\s"));
		assertEquals("SameStringTwice___(?:AndSome|SameStringTwice)",
				toRegexSorted(euchTree));
	}

	@Test
	public void testMidword() {
		final Word tree = WordHierarchyBuilder.createWordTree("aberi oberj"
				.split("\\s"));
		// assertEquals("(?:a|o)ber(?:i|j)", toRegexSorted(tree));
		assertEquals("aberi|oberj", toRegexSorted(tree));
	}

	@Test
	public void testPattern() {
		Pattern pattern = Pattern.compile("Sie");
		assertTrue(pattern.matcher("Sie").find());
		pattern = Pattern.compile("(Sie)");
		assertTrue(pattern.matcher("Sie").find());
		pattern = Pattern.compile("(?:Sie)");
		assertTrue(pattern.matcher("Sie").find());
		pattern = Pattern.compile("(?:Ihr|Sie)");
		assertTrue(pattern.matcher("Sie").find());
		assertTrue(pattern.matcher("Ihr").find());
	}

	@Test
	public void testCreatorIhr() {
		final String[] vforms = ihr;
		assertEquals(19, ihr.length);
		final HashSet<String> ihrSet = new HashSet<String>();
		for (final String str : vforms) {
			ihrSet.add(str);
		}
		final Word ihrTree = WordHierarchyBuilder.createWordTree(ihrSet);
		// System.out.println("result:" + ihrTree.myToString(true));
		final String regexStr = toRegex(ihrTree);
		final Pattern pattern = Pattern.compile(regexStr);
		assertTrue(pattern.matcher("Sie").find());
		for (final String str : vforms) {
			assertTrue(pattern.matcher(str).find());
		}
		assertFalse(pattern.matcher("").find());
		assertFalse(pattern.matcher("fix").find());
	}

	@Test
	public void testCreatorDu() {
		assertEquals(42, du.length);
		final Set<String> ihrSet = new HashSet<String>();
		ihrSet.addAll(Arrays.asList(du));
		final Word ihrTree = WordHierarchyBuilder.createWordTree(ihrSet);
		// System.out.println("result:" + ihrTree.myToString(true));
		final String regexStr = toRegex(ihrTree);
		final Pattern pattern = Pattern.compile(regexStr);
		assertTrue(pattern.matcher("Eurem").find());
		for (final String str : du) {
			assertTrue(pattern.matcher(str).find());
		}
		assertFalse(pattern.matcher("").find());
		assertFalse(pattern.matcher("fix").find());
	}

	@Test
	public void testTestIt() {
		final Set<String> ihrSet = new HashSet<String>();
		ihrSet.addAll(Arrays.asList(du));
		final Word ihrTree = WordHierarchyBuilder.createWordTree(ihrSet);
		assertTrue(ihrTree.testIt(ihrSet));
		ihrSet.add("garbage");
		assertFalse(ihrTree.testIt(ihrSet));
	}

	// This test runs for a while
	// after running it, do this in the shell (this runs even longer):
	// sort -u foo
	// expected output is:
	/*
	 *  1: 'Ih -'
	  2: 'nen '
	  2: 'r '
	   3: 'e '
	    4: 'm '
	    4: 'n '
	    4: 'r '
	    4: 's '
	     5: 'gleichen '
	     5: 'seits '
	result:0: 'null -'

	 */
	// @Test
	public void testIt() throws IOException {
		@SuppressWarnings("unused")
		final String[] ihr = new String[] { "Ihnen", "Ihr", "Ihre", "Ihrem",
				"Ihren", "Ihrer", "Ihrerseits", "Ihres", "Ihresgleichen",
				"Ihrethalben", "Ihretwegen", "Ihretwillen", "Ihrige",
				"Ihrigem", "Ihrigen", "Ihriger", "Ihriges", "Ihrs", "Sie", };
		final String[] ihr2 = new String[] { "Ihnen", "Ihr", "Ihre", "Ihrem",
				"Ihren", "Ihrer", "Ihrerseits", "Ihres", "Ihresgleichen", };

		final PermutationHelper<String> x = new PermutationHelper<String>(ihr2);

		final BufferedWriter out = new BufferedWriter(new FileWriter("foo"));

		for (final String[] result : x) {
			final Word ihrTree = WordHierarchyBuilder.createWordTree(result,
					out);

			out.write("result:" + ihrTree.myToString());
			out.write("\n");
		}
		out.close();
	}
	
	@Test
	public void testToStr() {
		final String input[]= "3112 3122 3132 31425 31".split("\\s");
		final Word tree = WordHierarchyBuilder.createWordTree(input);
		final String expected = " 31 \n  12 \n  22 \n  32 \n  425 \n";
		final String got = tree.myToStringSorted();
		assertEquals(expected, got);
	}
	
	@Test
	public void testToStr2() {
		final String input[]= "3112 3122 3132 31425".split("\\s");
		final Word tree = WordHierarchyBuilder.createWordTree(input);
		final String expected = " 31 -\n  12 \n  22 \n  32 \n  425 \n";
		final String got = tree.myToStringSorted();
		assertEquals(expected, got);
	}
	
	@Test
	public void testToStr3() {
		final String input[]= "aabcd aabce".split("\\s");
		final Word tree = WordHierarchyBuilder.createWordTree(input);
		final String expected = " aabc -\n  d \n  e \n";
		final String got = tree.myToStringSorted();
		assertEquals(expected, got);
	}
	
	@Test
	public void testToRegex() {
		final String input[]= "aabcd aabce".split("\\s");
		final Word tree = WordHierarchyBuilder.createWordTree(input);
		final String expected = "aabc[ed]";
		final String got = toRegex(tree);
		assertEquals(expected, got);
	}
	
	@Test
	public void testToRegex2() {
		final String input[]= "aabcder aabcdsie".split("\\s");
		final Word tree = WordHierarchyBuilder.createWordTree(input);
		final String expected1 = "aabcd(?:sie|er)";
		final String expected2 = "aabcd(?:er|sie)";
		final String got = toRegex(tree);
		assertTrue(got.equals(expected1) || got.equals(expected2));
	}

	
	/**
	 * Generates a reproducible regex matching all words in this tree.
	 * If you don't need reproducible ordering (mainly for testing),
	 * use see {@link Word#toRegex()} instead.
	 * 
	 * @return a reproducible regex matching all words in this tree
	 */
	public String toRegexSorted(final Word theWord) {
		final RegexWordProcessor wp = new RegexWordProcessor();
		theWord.processAll(wp, true);
		return wp.getResult();
	}

	/**
	 * Generates a regex matching all words in this tree.
	 * Note: for the same input the ordering in the regex may change!
	 * If you need reproducible ordering (mainly for testing),
	 * use see {@link Word#toRegexSorted()} instead.
	 * 
	 * @return a non-reproducible regex matching all words in this tree
	 */
	public static String toRegex(final Word theWord) {
		final RegexWordProcessor wp = new RegexWordProcessor();
		theWord.processAll(wp);
		return wp.getResult();
	}
}
