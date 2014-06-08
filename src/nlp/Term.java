package nlp;

/**
 * @author: OmerTanwirHassan
 * Date: 6/8/14
 */
public class Term {
    String text;
    String label;
    String pos;

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public Term(String text, String label, String pos) {
        this.text = text;
        this.label = label;
        this.pos = pos;
    }

    public Term(String text, String label) {
        this.text = text;

        this.label = label;
    }

    public String getText() {

        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
