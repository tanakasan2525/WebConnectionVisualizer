package nettest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;


public class MarkovChain {
	private Tokenizer _tokenizer = Tokenizer.builder().build();
	private TokenMap<MarkovToken, String> _tokenMap = new TokenMap<MarkovToken, String>();

	int N_th_order;		//	マルコフ連鎖の次元数

	MarkovChain(int n_th_order) {
		this.N_th_order = n_th_order;
	}

	/**
	 * 文を追加します。
	 * @param sentence	文
	 */
	public void add(String sentence) {
		List<Token> tokens = _tokenizer.tokenize(sentence);
		if (tokens.size() >= N_th_order) {
			MarkovToken mTokens = new MarkovToken(N_th_order);
			for (int i = 0; i < tokens.size(); ++i) {
				_tokenMap.put(mTokens, tokens.get(i).getSurfaceForm());
				mTokens = mTokens.createNext(tokens.get(i).getSurfaceForm());
			}
			_tokenMap.put(mTokens, null);		//	文の終わり
		}
	}

	/**
	 * 二次のマルコフ連鎖で文章を生成します。
	 * @return
	 */
	public String generate() {
		StringBuilder ret = new StringBuilder();
		if (_tokenMap.size() >= N_th_order) {
			String token;
			MarkovToken mTokens = new MarkovToken(N_th_order);
			for (token = _tokenMap.getRandomly(mTokens); token != null; token = _tokenMap.getRandomly(mTokens)) {
				ret.append(token);
				mTokens.next(token);
			}
		}
		return ret.toString();
	}

	/**
	 * 蓄積した文をすべて削除します。
	 */
	public void clear() {
		_tokenMap.clear();
	}
}

class MarkovToken{
	public ArrayList<String> tokens;
	MarkovToken(int n) {
		tokens = new ArrayList<String>();
		for (int i = 0; i < n; ++i)
			tokens.add("");
	}

	MarkovToken(MarkovToken mToken) {
		tokens = new ArrayList<String>(mToken.tokens);
	}

	MarkovToken createNext(String nextToken) {
		MarkovToken mToken = new MarkovToken(this);
		mToken.next(nextToken);
		return mToken;
	}

	void next(String nextToken) {
		tokens.remove(0);
		tokens.add(nextToken);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(MarkovToken.class)) {
			if (tokens.size() != ((MarkovToken)obj).tokens.size()) return false;
			for (int i = 0; i < tokens.size(); ++i) {
				if (tokens.get(i) == null && ((MarkovToken)obj).tokens.get(i) == null) continue;
				if (tokens.get(i) == null || ((MarkovToken)obj).tokens.get(i) == null) return false;
				if (!tokens.get(i).equals(((MarkovToken)obj).tokens.get(i))) return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode(){
		int hash = 0;
		for (int i = 0; i < tokens.size(); ++i)
			hash += tokens.get(i) == null ? 0 : tokens.get(i).hashCode();
        return hash;
    }
}

/**
 * 	キーの重複を許可したトークン用のハッシュマップ
 * @author a5812070
 *
 * @param <Key>
 * @param <Value>
 */
class TokenMap<Key, Value>{
	private HashMap<Key, ArrayList<Value>> _map = new HashMap<Key, ArrayList<Value>>();
	private Random _rnd = new Random(System.currentTimeMillis());

	/**
	 * キーを設定して、値を格納します。
	 * @param key	キー
	 * @param value	値
	 */
	public void put(Key key, Value value) {
		ArrayList<Value> set = _map.get(key);
		if (set == null) {
			set = new ArrayList<Value>();
		}
		set.add(value);
		_map.put(key,  set);
	}

	/**
	 * キーに対応する値のリストを取得する
	 * @param key	キー
	 * @return		値のリスト
	 */
	public List<Value> get(Key key) {
		return _map.get(key);
	}

	/**
	 * キーに対応する値のリストからランダムに１つ値を取得する
	 * @param key	キー
	 * @return		値
	 */
	public Value getRandomly(Key key) {
		List<Value> lst = get(key);
		if (lst == null) return null;
		return lst.get(_rnd.nextInt(lst.size()));
	}

	/**
	 * すべてのマッピングをマップから削除します。この呼び出しが戻ると、マップは空になります。
	 */
	public void clear() {
		_map.clear();
	}

	public int size() {
		return _map.size();
	}
}
