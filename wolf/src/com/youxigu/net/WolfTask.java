package com.youxigu.net;

import java.io.Serializable;

public interface WolfTask extends Serializable{
	WolfTask execute(Response response);
}
