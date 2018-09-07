package ${package};

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * ${class_inform}
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 * 
 * @author system
 */
public class ${class_name} {
	
	<#-- 变量 -->
	<#list vars as var> 
	// ${var.variable_desc!""}
	private ${var.variable_type} ${var.variable_naming};
    </#list>
    
   	<#list vars as var> 
	public ${var.variable_type} get${var.variable_naming?cap_first}() {
		return ${var.variable_naming};
	}
	
    </#list>
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("${class_name} [");
		<#list vars as var>
		<#if var_index == 0>
		builder.append("${var.variable_naming}=");
		<#else>
		builder.append(", ${var.variable_naming}=");
		</#if>
		builder.append(${var.variable_naming});
		</#list>
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * 清空已有的数据缓存
	 */
	public static void clearCache() {
		DATA.clearCache();
	}
	
	/**
	 * 数据重载
	 * @param  dataId
	 * @return 失败返回失败的Id，全部成功返回null
	 */
	public static String reload(int... dataId) {
		final List<Integer> dataIdList = new ArrayList<>(dataId.length);
		for (int tmp : dataId) {
			dataIdList.add(tmp);
		}
		return dataIdList.isEmpty() ? "" : DATA.reload(dataIdList);
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<${class_name}> getAllData() {
		return DATA.getList();
	}
	
	/**
	 * 通过ID获取数据
	 * @param id
	 * @return
	 */
	public static ${class_name} getDataById(int id) {
		return DATA.getMap().get(id);
	}
	
	/**
	 * 通过多个属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ${class_name} getDataBy(Object... params) {
		List<${class_name}> list = filter(params);
		// 如果满足条件的结果不唯一
		if (list.size() > 1) {
			throw new RuntimeException(String.format("数据条大于1条.查询结果长度为%s", list.size()));
		}
		
		return !list.isEmpty() ? list.get(0) : null;
	}
	
	/**
	 * 通过多个属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<${class_name}> getListBy(Object... params) {
		return filter(params);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	private static List<${class_name}> filter(Object... params) {
		// 参数必须成对出现
		if (params.length % 2 != 0) 
			throw new IllegalArgumentException(String.format("查询参数必须成对出现,参数长度=%s", params.length));
		
		List<${class_name}> results = new ArrayList<>(${class_name}.getAllData());
		List<${class_name}> filterResults = new ArrayList<>(); 
		// 处理成对参数
		for (int i = 0; i < params.length; i += 2) {
		    Var varType = (Var) params[i];
			Object value = params[i + 1];
			filterResults = results.parallelStream()
			     .filter(data -> data.isEqual(data.getValue(varType), value))
			     .collect(Collectors.toList()); 
	    	results = filterResults;
		}
		
		return results;
	}
	
    /**
	 * 比较值
	 * @param value1
	 * @param value2
	 * @return
	 */
	private boolean isEqual(Object value1, Object value2) {
	    if (value1 == null || value2 == null)
			return false;
		if (value1.getClass() != value2.getClass())
			throw new IllegalArgumentException(String.format("查询参数不属于同一对象声明:value1=%s,value2=%s", value1.getClass().getSimpleName(), value2.getClass().getSimpleName()));
		
		return value1.equals(value2);
	}
	
    /**
	 * 返回对象对应的变量值
	 * @param fieldName
	 * @return
	 */
	private Object getValue(Var varType) {
		switch (varType) {
		    <#list vars as var>
		    case ${var.variable_naming}:
		        return this.${var.variable_naming};
		    </#list>
			default:
				throw new IllegalArgumentException("无效的变量名：" + varType);
		}
	}
	
	/**
	 * 属性关键字
	 */
	public static enum Var {
	    <#list vars as var>
	    // ${var.variable_desc!""}
	    ${var.variable_naming},
		</#list>
		;
	}
	
	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	private static final class DATA {
	
		// 全部数据
		private static volatile Map<Integer, ${class_name}> dataMap;

		/**
		 * 清空已有的数据缓存
		 */
		public static void clearCache() {
			dataMap = null;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<${class_name}> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ${class_name}> getMap() {
			// 延迟初始化
			if(dataMap == null) {
				synchronized (DATA.class) {
					if(dataMap == null) {
						init();
					}
				}
			}
			
			return dataMap;
		}
		
		/**
		 * 初始化数据
		 */
		private static void init() {
			// JSON数据
			String confJSON = readConfFile();
			if (StringUtils.isBlank(confJSON)) 
				return;
			
			// 填充实体数据
			JSONArray confs = JSONArray.parseArray(confJSON);
			Map<Integer, ${class_name}> allDataMap = new ConcurrentHashMap<>(confs.size());
			for (int i = 0; i < confs.size(); i++) {
				JSONObject conf = confs.getJSONObject(i);
		        ${class_name} rntObj = toGDItem(conf);
				
				allDataMap.put(conf.getInteger("id"), rntObj);
			}
            DATA.dataMap = allDataMap;
		}
		
		/**
		 * 读取游戏配置
		 */
		private static String readConfFile() {
			try {	
			    // 获取json文件的URI	
				URI baseBath = Thread.currentThread().getContextClassLoader().getResource("gamedata/${class_name}.json").toURI();
				
				// 读取文件
				Path path = Paths.get(baseBath);
				byte[] bytes = Files.readAllBytes(path);
				return new String(bytes, "UTF-8");
			} catch(IOException | URISyntaxException e) {
				throw new RuntimeException(String.format("文件：%s,异常信息：%s, ", "${class_name}.json", e.getCause()));
			}
		}
		
		/**
		 * 转换对象
		 * @param jsonObj
		 */
		private static ${class_name} toGDItem(JSONObject jsonObj) {
			${class_name} gd = new ${class_name}();
		   	<#list vars as var> 
			gd.${var.variable_naming} = jsonObj.getObject("${var.variable_naming}", ${var.variable_type}.class);
		    </#list>
		    return gd;
		}
		
		/**
	     * 重新装载此数据对象，并初始化之，且替换此对象的的缓存
	     * @param dataIds
	     * @return
	     */
		public static String reload(List<Integer> dataIds) {
			// JSON数据
			String confJSON = readConfFile();
			if (StringUtils.isBlank(confJSON)) 
				throw new RuntimeException(String.format("读取空数据,%s", "${class_name}.json"));
			
			// 填充实体数据
			JSONArray confs = JSONArray.parseArray(confJSON);
			for (int i = 0; i < confs.size(); i++) {
				if (dataIds.isEmpty())
					break;
				
				JSONObject conf = confs.getJSONObject(i);
				Integer id = conf.getInteger("id");
				if (dataIds.contains(id)) {
					${class_name} source = toGDItem(conf);
					${class_name} target = getMap().get(id);
					// 判断缓存中是否存在，存在则修改，不存在则添加
					if (target != null) {
						copyTo(source, target);
					} else {
						getMap().put(id, source);
					}
					dataIds.remove(id);
				}
			}
	        
			return !dataIds.isEmpty() ? dataIds.toString() : "";
		}
		
		/**
		 * 复制source数据给traget
		 * @param source
		 * @param target
		 */
		private static void copyTo(${class_name} source, ${class_name} target) {
		   	<#list vars as var> 
			target.${var.variable_naming} = source.${var.variable_naming};
		    </#list>
		}
		
	}
	
}