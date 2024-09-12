package com.inspur.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @projectName: platform_app_service
 * @package: com.inspur.page
 * @className: PageBean
 * @author: kliu
 * @description: 分页
 * @date: 2022/4/18 10:32
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageBean {
    /**
     * 内容列表
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private List contentList;
    /**
     * 每页大小
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private int pageSize ;
    /**
     * list中元素有多少个
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private int listTotalSize;
    /**
     * 当前页数
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private int currentpage;
    /**
     * 总页数
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private int totalPage;
    /**
     * 总数量
     * @author kliu
     * @date 2022/6/7 15:25
     */
    private int totalSize;
}
