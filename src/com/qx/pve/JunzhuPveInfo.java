package com.qx.pve;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name = "JunzhuPveInfo")
public class JunzhuPveInfo implements MCSupport,DBHash{
		
	    
	    /**
	 * 
	 */
	   private static final long serialVersionUID = 1L;
		@Id
		public long junzhuId;
		@Column(columnDefinition="INT default 0")
		public int commonChptMaxId;//君主最大普通关卡
		@Column(columnDefinition="INT default 0")
		public int legendChptMaxId;//君主最大传奇关卡
		public int starCount;
		public JunzhuPveInfo(long junzhuId) {
			this.junzhuId = junzhuId;
		}
		public JunzhuPveInfo() {
			
		}
		@Override
		public long hash() {
			return junzhuId;
		}
		@Override
		public long getIdentifier() {
			return junzhuId;
		}
}