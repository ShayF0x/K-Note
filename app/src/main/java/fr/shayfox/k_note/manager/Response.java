package fr.shayfox.k_note.manager;

public class Response {
    private final String _content;
    private final int _code;

    public Response(String _content, int _code) {
        this._content = _content;
        this._code = _code;
    }

    public String get_content() {
        return _content;
    }

    public int get_code() {
        return _code;
    }
}
