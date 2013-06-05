package base;

public class StrEncoder {

	/**
	 * Encode a string to 'safe' form, one with appropriate escape codes inserted,
	 * and optional quoting
	 * @param raw : the raw string to be encoded
	 * @param StringBuilder : the StringBuilder to append to
	 */
	public void encode(StringBuilder sb, CharSequence raw) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Decode a string from 'safe' form
	 * @param encoded : the string to be decoded
	 * @param StringBuilder : the StringBuilder to append to
	 */
	public void decode(StringBuilder sb, CharSequence encoded) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Decode a string from 'safe' form to its appropriate raw form
	 * 
	 * @param encoded : the 'safe' form
	 * @return raw string
	 */
	public String decode(CharSequence encoded) {
		StringBuilder sb = new StringBuilder();
		decode(sb, encoded);
		return sb.toString();
	}

	public String encode(CharSequence raw) {
		StringBuilder s = new StringBuilder();
		encode(s, raw);
		return s.toString();
	}

}
