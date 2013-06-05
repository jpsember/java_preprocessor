package jpp;

import base.*;

public class JavaStrEncoder 
	 extends StrEncoder {

	public static StrEncoder encoder = new JavaStrEncoder();
	
		 private JavaStrEncoder() {}

		 
			public void encode(StringBuilder s,CharSequence raw  ) {
				for (int i = 0 ; i < raw.length(); i++) {
					char c = raw.charAt(i);
					
					switch (c) {
					case '\n':
						s.append("\\n");
						break;
					case '\\':
						s.append("\\\\");
						break;
					case '"':
						s.append("\\\"");
							break;
						
					case ' ':
						s.append(c);
						break;
					case '\t':
						s.append(' ');
						break;
					default:
						if (c > 0x20 && c < 0x80)
							s.append(c);
						else {
							s.append("\\u");
							s.append(TextScanner.toHex(c,4));
						}
					break;
					
//					case '\\':
//						i++;
//						if (i == raw.length()) throw new IllegalArgumentException(raw);
//						c = raw.charAt(i);
//						switch (c) {
//						case 'n':
//							c = '\n';
//							break;
//						}
//						break;
						
					} 
				}
			}


//		public String encode(String raw) {
//			StringBuilder s = new StringBuilder();
//			encode(raw,s);
//			return s.toString();
//		}
//
		/**
		 * Decode string from java form
		 */
		public void decode(StringBuilder raw,CharSequence encoded) {
			for (int i = 0 ; i < encoded.length(); i++) {
				char c = encoded.charAt(i);
				
				switch (c) {
				default:
					raw.append(c);
				break;
				
				case '\\':
					i++;
					if (i == encoded.length()) throw new IllegalArgumentException(encoded.toString());
					c = encoded.charAt(i);
					switch (c) {
					case 'n':
						c = '\n';
						break;
					}
					raw.append(c);
					break;
				} 
			}
		}

}
