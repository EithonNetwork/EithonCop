package net.eithon.plugin.cop.profanity;

class Leet {
	
	public static boolean isLeet(String message) {
		return !message.equalsIgnoreCase(decode(message));
	}
	
	static String decode(String message) {
		StringBuilder result = new StringBuilder();
		char[] input = message.toCharArray();
		char newC;
		for (int i = 0; i < input.length; i++) {
			char c1 = input[i];
			char c2 = i+1 < input.length ? input[i+1] : ' ';
			char c3 = i+2 < input.length ? input[i+2] : ' ';
			switch (c1) {
			case '4':
			case '@':
			case '^':
			case '*':
				newC = 'a';
				break;
			case '8':
				newC = 'b';
				break;
			case '(':
			case '<':
				newC = 'c';
				break;
			case '3':
				newC = 'e';
				break;
			case '6':
			case '&':
				newC = 'g';
				break;
			case '#':
				newC = 'h';
				break;
			case '1':
			case '!':
				newC = 'i';
				break;
			case '0':
				newC = 'o';
				break;
			case '5':
			case '$':
				newC = 's';
				break;
			case '7':
			case '+':
				newC = 't';
				break;
			case '%':
				newC = 'x';
				break;
			case '[':
				switch (c2) {
				case ')':
					newC = 'd';
					i++;
					break;
				default:
					newC = c1;
					break;
				}
				break;
			case '/':
				switch (c2) {
				case '\\':
					newC = 'a';
					i++;
					break;
				case '-':
					if (c3 == '\\') {
						newC = 'a';
						i += 2;
					} else newC = c1;
					break;
				default:
					newC = c1;
					break;
				}
				break;
			case '\\':
				if (c2 == '/') {
					newC = 'v';
					i++;
					break;
				} else newC = c1;
				break;
			default:
				newC = c1;
			}
			result.append(newC);
		}
		return result.toString();
	}
	
	public static String encode(String message) {
		StringBuilder result = new StringBuilder();
		char[] input = message.toCharArray();
		char newC;
		for (int i = 0; i < input.length; i++) {
			char c1 = input[i];
			switch (c1) {
			case 'a':
				newC = '4';
				break;
			case 'b':
				newC = '8';
				break;
			case 'e':
				newC = '3';
				break;
			case 'g':
				newC = '6';
				break;
			case 'h':
				newC = '#';
				break;
			case 'i':
				newC = '1';
				break;
			case 'o':
				newC = '0';
				break;
			case 's':
				newC = '5';
				break;
			case 't':
				newC = '7';
				break;
			default:
				newC = c1;
			}
			result.append(newC);
		}
		return result.toString();
	}
}