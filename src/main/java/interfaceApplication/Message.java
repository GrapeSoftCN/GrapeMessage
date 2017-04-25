package interfaceApplication;

import java.util.HashMap;

import org.json.simple.JSONObject;

import esayhelper.JSONHelper;
import esayhelper.TimeHelper;
import filterword.WordFilter;
import model.MessageModel;
import rpc.execRequest;

public class Message {
	private MessageModel model = new MessageModel();
	private HashMap<String, Object> map = new HashMap<>();
	private JSONObject _obj = new JSONObject();
//	private static int userPlv;
//	
//	static{
//		userPlv = Integer.parseInt(execRequest._run("GrapeAuth/Auth/getUserPlv", null).toString());
//	}
	public Message() {
		map.put("messageId", model.getID());
		map.put("messageDate", TimeHelper.nowMillis() + "");
		map.put("floor", model.getFloor());
		map.put("fatherid", 0);
		map.put("oid", "0"); // 文章id
		map.put("state", 0); // 状态
		map.put("replynum", 0); // 留言被回复多少次
		map.put("rPlv", 1000);  //读取  权限值
		map.put("uPlv", 2000);  //修改  权限值
		map.put("dPlv", 3000);  //删除  权限值
	}

	/**
	 * 新增留言及回复留言
	 * 
	 * @param msgInfo
	 * @return 若是新增留言，则存在字段floor，fatherid默认为0，
	 *         若为回复留言，则不存在floor字段，fatherid为需回复的留言的id
	 */
	@SuppressWarnings("unchecked")
	public String AddMessage(String msgInfo) {
		int code = 0;
		JSONObject result=new JSONObject();
		JSONObject object = model.AddMap(map, JSONHelper.string2json(msgInfo));
		if (object.containsKey("fatherid")) {
			object.put("fatherid", object.get("fatherid").toString());
			if (!object.get("fatherid").toString().equals("0")) {
				object.remove("floor");
				String replynum = String.valueOf(model.countReply(object.get("fatherid")
						.toString()));
				JSONObject object2 = model.find(object.get("fatherid").toString());
				object2.put("replynum", Integer.parseInt(replynum));
				code = model.updateMessage(object.get("fatherid").toString(), object2);
			}
		}
		if (code==0) {
			result = JSONHelper.string2json(model.addMessage(object));
		}
		_obj.put("records", result);
		return model.resultMessage(0, _obj.toString());
	}

	// 修改留言
	public String UpdateMessage(String mid, String msgInfo) {
		
		return model.resultMessage(model.updateMessage(mid, JSONHelper.string2json(msgInfo)),
				"留言修改成功");
	}

	// 删除留言
	public String DeleteMessage(String mid) {
		return model.resultMessage(model.deleteMessage(mid), "删除留言成功");
	}

	// 批量删除留言
	public String DeleteBatchMessage(String mids) {
		return model.resultMessage(model.deleteMessage(mids.split(",")), "批量删除留言成功");
	}

	public String MaskMessage(String mid){
		return model.resultMessage(model.deletesMessage(mid), "隐藏留言或回复成功");
	}
	// 搜索留言
	@SuppressWarnings("unchecked")
	public String SearchMessage(String msgInfo) {
		_obj.put("records", model.find(JSONHelper.string2json(msgInfo)));
		return model.resultMessage(0, _obj.toString());
	}

	// 分页
	@SuppressWarnings("unchecked")
	public String PageMessage(int idx, int pageSize) {
		_obj.put("records", model.page(idx, pageSize));
		return model.resultMessage(0, _obj.toString());
	}

	// 条件分页
	@SuppressWarnings("unchecked")
	public String PageByMessage(int idx, int pageSize, String msgInfo) {
		_obj.put("records", model.page(idx, pageSize, JSONHelper.string2json(msgInfo)));
		return model.resultMessage(0, _obj.toString());
	}

	// 获取某篇文章下的留言
	@SuppressWarnings("unchecked")
	public String getMsgByOid(String oid) {
		_obj.put("records", model.FindMsgByOID(oid));
		return model.resultMessage(0, _obj.toString());
	}

	// 将某一条留言关联至某篇文章
	public String connArc(String mid, String oid) {
		return model.resultMessage(model.setMsgConOID(mid, oid), "文章关联留言成功");
	}

	/**
	 * 检测是否存在敏感词
	 * @param string
	 * @return
	 *  true表示存在敏感词，false表示不存在敏感词
	 */
	public String CheckMessage(String string) {
		return model.resultMessage(0, String.valueOf(WordFilter.isContains(string)));
	}
}
