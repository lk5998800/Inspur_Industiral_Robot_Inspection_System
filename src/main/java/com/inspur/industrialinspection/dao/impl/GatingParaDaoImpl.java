package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.GatingParaDao;
import com.inspur.industrialinspection.entity.GatingPara;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 门控参数dao实现
 * @author kliu
 * @date 2022/6/23 19:14
 */
@Repository
public class GatingParaDaoImpl implements GatingParaDao {
    @Autowired
    private BeanFactory beanFactory;

    @Override
    public GatingPara getDetlById(long roomId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from gating_para ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, pointName);
        List<GatingPara> list = db.dbQuery(GatingPara.class);
        if (list.size() == 0) {
            throw new RuntimeException("依据机房ID【"+roomId+"】点位【"+pointName+"】获取门控参数失败");
        }
        return list.get(0);
    }

    /**
     * 添加门控参数
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    @Override
    public void add(GatingPara gatingPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into industrial_robot.gating_para ");
        stringBuffer.append("  (point_name, door_code, request_order, room_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, gatingPara.getPointName());
        db.set(index++, gatingPara.getDoorCode());
        db.set(index++, gatingPara.getRequestOrder());
        db.set(index++, gatingPara.getRoomId());
        db.dbUpdate();
    }

    /**
     * 更新门控参数
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    @Override
    public void update(GatingPara gatingPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update industrial_robot.gating_para ");
        stringBuffer.append("   set door_code = ?, request_order = ? ");
        stringBuffer.append(" where point_name = ? ");
        stringBuffer.append("   and room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, gatingPara.getDoorCode());
        db.set(index++, gatingPara.getRequestOrder());
        db.set(index++, gatingPara.getPointName());
        db.set(index++, gatingPara.getRoomId());
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     *
     * @param gatingPara
     * @return boolean
     * @author kliu
     * @date 2022/7/11 15:46
     */
    @Override
    public boolean checkExist(GatingPara gatingPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from gating_para ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, gatingPara.getRoomId());
        db.set(index++, gatingPara.getPointName());
        List<GatingPara> list = db.dbQuery(GatingPara.class);
        return list.size()>0;
    }

    @Override
    public GatingPara getDetlByDoorCode(String doorCode) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from gating_para ");
        stringBuffer.append(" where door_code = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, doorCode);
        List<GatingPara> list = db.dbQuery(GatingPara.class);
        if (list.size() == 0) {
            throw new RuntimeException("依据门控id【"+doorCode+"】获取门控参数失败");
        }
        return list.get(0);
    }
    /**
     * 根据单个点位获取所有门控参数
     * @param roomId
     * @param pointName
     * @return List<GatingPara>
     * @author ldh
     * @date 2022/10/25
     */
    @Override
    public List<GatingPara> getDetlsById(long roomId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from gating_para ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name like ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, pointName.substring(0,pointName.length()-3)+"%");
        List<GatingPara> list = db.dbQuery(GatingPara.class);
        if (list.size() == 0) {
            throw new RuntimeException("依据机房ID【"+roomId+"】点位【"+pointName+"】获取门控参数失败");
        }
        return list;
    }
}
