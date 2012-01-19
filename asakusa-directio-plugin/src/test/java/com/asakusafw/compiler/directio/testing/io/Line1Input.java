package com.asakusafw.compiler.directio.testing.io;
import com.asakusafw.compiler.directio.testing.model.Line1;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>line1</code>を読み出す
 */
public final class Line1Input implements ModelInput<Line1> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public Line1Input(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Line1 model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getValueOption());
        parser.fill(model.getFirstOption());
        parser.fill(model.getPositionOption());
        parser.fill(model.getLengthOption());
        parser.endRecord();
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}