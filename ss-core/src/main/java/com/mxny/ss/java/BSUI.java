package com.mxny.ss.java;

public interface BSUI {
	Object s(String s, Object obj);
	Object g(String s);
	Object e(String code);
	Object ef(String path);
	Object ex(String code) throws Exception;
	Object dae(String code, String key);
	Object dae(String code);
	Object daex(String code, String key) throws Exception;
	Object daex(String code) throws Exception;
	Object sc(String sc);
	Object scx(String sc) throws Exception;
	int r(int seed);
}
