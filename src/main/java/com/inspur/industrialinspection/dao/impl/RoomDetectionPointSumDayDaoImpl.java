package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RoomDetectionPointSumDayDao;
import com.inspur.industrialinspection.entity.RoomDetectionPointSumDay;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 机房检测项按日统计dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class RoomDetectionPointSumDayDaoImpl implements RoomDetectionPointSumDayDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public void add(RoomDetectionPointSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into room_detection_point_sum_day ");
        stringBuffer.append("  (room_id, detection_date, detection_id, max, min, ");
        stringBuffer.append("   abnormal_count, count, point_name, max_abnormal, min_abnormal) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.set(index++, roomDetectionSumDay.getMax());
        db.set(index++, roomDetectionSumDay.getMin());

        db.set(index++, roomDetectionSumDay.getAbnormalCount());
        db.set(index++, roomDetectionSumDay.getCount());
        db.set(index++, roomDetectionSumDay.getPointName());
        db.set(index++, roomDetectionSumDay.getMaxAbnormal());
        db.set(index++, roomDetectionSumDay.getMinAbnormal());
        db.dbUpdate();

    }

    /**
     * 更新
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public void update(RoomDetectionPointSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update room_detection_point_sum_day ");
        stringBuffer.append("   set max = ?, min = ?, abnormal_count = ?, count = ?, max_abnormal = ?, min_abnormal = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getMax());
        db.set(index++, roomDetectionSumDay.getMin());
        db.set(index++, roomDetectionSumDay.getAbnormalCount());
        db.set(index++, roomDetectionSumDay.getCount());
        db.set(index++, roomDetectionSumDay.getMaxAbnormal());
        db.set(index++, roomDetectionSumDay.getMinAbnormal());

        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.set(index++, roomDetectionSumDay.getPointName());
        db.dbUpdate();


    }

    /**
     * 添加
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public boolean checkExist(RoomDetectionPointSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from room_detection_point_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.set(index++, roomDetectionSumDay.getPointName());
        return db.dbQuery().size()>0;
    }

    /**
     * 查询明细
     * @param roomDetectionSumDay
     * @return com.inspur.industrialinspection.entity.RoomDetectionPointSumDay
     * @author kliu
     * @date 2022/5/24 18:25
     */
    @Override
    public RoomDetectionPointSumDay getDetlById(RoomDetectionPointSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_point_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.set(index++, roomDetectionSumDay.getPointName());
        return (RoomDetectionPointSumDay) db.dbQuery(RoomDetectionPointSumDay.class).get(0);
    }

    /**
     * 依据机房id和日期获取数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionPointSumDay>
     * @author kliu
     * @date 2022/5/24 18:26
     */
    @Override
    public List<RoomDetectionPointSumDay> list(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_point_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        return db.dbQuery(RoomDetectionPointSumDay.class);
    }
    /**
     * 依据机房id和日期获取数据，大于等于日期
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionPointSumDay>
     * @author kliu
     * @date 2022/6/25 17:43
     */
    @Override
    public List<RoomDetectionPointSumDay> listGteDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_point_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date >= ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        return db.dbQuery(RoomDetectionPointSumDay.class);
    }
}
