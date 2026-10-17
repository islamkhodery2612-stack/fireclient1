package com.fireclient.replica;

/**
 * Small math evaluator for /fcalc.
 * Supports + - * / % ^ ( ), the multiplication sign x, and item-count suffixes:
 * s = stack (64), sb = shulker box of full stacks (1728), dc = double chest (3456).
 */
public final class Calc {
	private final String src;
	private int pos;

	private Calc(String src) {
		this.src = src;
	}

	public static double eval(String expression) {
		Calc c = new Calc(expression);
		double v = c.parseExpr();
		c.skipSpaces();
		if (c.pos < c.src.length()) {
			throw new IllegalArgumentException("Unexpected '" + c.src.charAt(c.pos) + "'");
		}
		return v;
	}

	public static String describe(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return "not a number";
		}
		if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			long n = (long) value;
			if (n >= 64) {
				long boxes = n / 1728;
				long stacks = (n % 1728) / 64;
				long items = n % 64;
				StringBuilder sb = new StringBuilder().append(n).append(" = ");
				boolean first = true;
				if (boxes > 0) {
					sb.append(boxes).append(" shulker box").append(boxes == 1 ? "" : "es");
					first = false;
				}
				if (stacks > 0) {
					sb.append(first ? "" : " + ").append(stacks).append(" stack").append(stacks == 1 ? "" : "s");
					first = false;
				}
				if (items > 0) {
					sb.append(first ? "" : " + ").append(items).append(items == 1 ? " item" : " items");
				}
				return sb.toString();
			}
			return Long.toString(n);
		}
		return String.format(java.util.Locale.ROOT, "%.4f", value);
	}

	private void skipSpaces() {
		while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
			pos++;
		}
	}

	private boolean eat(char c) {
		skipSpaces();
		if (pos < src.length() && src.charAt(pos) == c) {
			pos++;
			return true;
		}
		return false;
	}

	private double parseExpr() {
		double v = parseTerm();
		while (true) {
			if (eat('+')) {
				v += parseTerm();
			} else if (eat('-')) {
				v -= parseTerm();
			} else {
				return v;
			}
		}
	}

	private double parseTerm() {
		double v = parsePower();
		while (true) {
			if (eat('*') || eat('x') || eat('X')) {
				v *= parsePower();
			} else if (eat('/')) {
				v /= parsePower();
			} else if (eat('%')) {
				v %= parsePower();
			} else {
				return v;
			}
		}
	}

	private double parsePower() {
		double base = parseUnary();
		if (eat('^')) {
			return Math.pow(base, parsePower());
		}
		return base;
	}

	private double parseUnary() {
		if (eat('-')) {
			return -parseUnary();
		}
		if (eat('+')) {
			return parseUnary();
		}
		return parsePrimary();
	}

	private double parsePrimary() {
		if (eat('(')) {
			double v = parseExpr();
			if (!eat(')')) {
				throw new IllegalArgumentException("Missing ')'");
			}
			return v;
		}
		skipSpaces();
		int start = pos;
		while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
			pos++;
		}
		if (start == pos) {
			throw new IllegalArgumentException("Expected a number");
		}
		double v = Double.parseDouble(src.substring(start, pos));
		return v * parseSuffix();
	}

	private double parseSuffix() {
		if (suffixAt("sb")) {
			pos += 2;
			return 1728;
		}
		if (suffixAt("dc")) {
			pos += 2;
			return 3456;
		}
		if (suffixAt("s")) {
			pos += 1;
			return 64;
		}
		return 1;
	}

	private boolean suffixAt(String s) {
		if (!src.regionMatches(true, pos, s, 0, s.length())) {
			return false;
		}
		int next = pos + s.length();
		return next >= src.length() || !Character.isLetter(src.charAt(next));
	}
}
