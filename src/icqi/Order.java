package icqi;

public enum Order {
	LOGIN {
		final String o = "$LOGIN$";

		public String toString() {
			return o;
		}

	},
	OFFLINE {
		final String o = "$OFFLINE$";

		public String toString() {
			return o;
		}
	},
	FILE_UPLOAD {
		final String o = "$FILE_UPLOAD$";

		public String toString() {
			return o;
		}
	},
	FILE_DOWNLOAD {
		final String o = "$FILE_DOWNLOAD$";

		public String toString() {
			return o;
		}
	},
	MESSAGE {
		final String o = "$MESSAGE$";

		public String toString() {
			return o;
		}
	},
	RENAME{
		final String o = "$RENAME$";

		public String toString() {
			return o;
		}
	};

	@Override
	public String toString() {
		return this.toString();
	}

	public byte[] getBytes() {
		return this.toString().getBytes();
	}

}
