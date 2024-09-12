package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RoomDetectionSumDayDao;
import com.inspur.industrialinspection.entity.RoomDetectionSumDay;
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
public class RoomDetectionSumDayDaoImpl implements RoomDetectionSumDayDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public void add(RoomDetectionSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into room_detection_sum_day ");
        stringBuffer.append("  (room_id, detection_date, detection_id, avg, max, ");
        stringBuffer.append("   min, median, abnormal_count, count) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.set(index++, roomDetectionSumDay.getAvg());
        db.set(index++, roomDetectionSumDay.getMax());

        db.set(index++, roomDetectionSumDay.getMin());
        db.set(index++, roomDetectionSumDay.getMedian());
        db.set(index++, roomDetectionSumDay.getAbnormalCount());
        db.set(index++, roomDetectionSumDay.getCount());
        db.dbUpdate();

    }

    /**
     * 更新
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public void update(RoomDetectionSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update room_detection_sum_day ");
        stringBuffer.append("   set avg = ?, max = ?, min = ?, median = ?, abnormal_count = ?, count = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getAvg());
        db.set(index++, roomDetectionSumDay.getMax());
        db.set(index++, roomDetectionSumDay.getMin());
        db.set(index++, roomDetectionSumDay.getMedian());
        db.set(index++, roomDetectionSumDay.getAbnormalCount());
        db.set(index++, roomDetectionSumDay.getCount());

        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        db.dbUpdate();


    }

    /**
     * 添加
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    @Override
    public boolean checkExist(RoomDetectionSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from room_detection_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        return db.dbQuery().size()>0;
    }

    /**
     * 查询明细
     * @param roomDetectionSumDay
     * @return com.inspur.industrialinspection.entity.RoomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:25
     */
    @Override
    public RoomDetectionSumDay getDetlById(RoomDetectionSumDay roomDetectionSumDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        stringBuffer.append("   and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomDetectionSumDay.getRoomId());
        db.set(index++, roomDetectionSumDay.getDetectionDate());
        db.set(index++, roomDetectionSumDay.getDetectionId());
        return (RoomDetectionSumDay) db.dbQuery(RoomDetectionSumDay.class).get(0);
    }

    /**
     * 依据机房id和日期获取数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionSumDay>
     * @author kliu
     * @date 2022/5/24 18:26
     */
    @Override
    public List<RoomDetectionSumDay> list(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        return db.dbQuery(RoomDetectionSumDay.class);
    }
    /**
     * 依据机房id和日期获取数据，大于等于日期
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionSumDay>
     * @author kliu
     * @date 2022/6/25 17:43
     */
    @Override
    public List<RoomDetectionSumDay> listGteDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and detection_date >= ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        return db.dbQuery(RoomDetectionSumDay.class);
    }

    /**
     * 删除机房统计数据
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/7/29 11:01
     */
    @Override
    public void deleteAll(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from room_detection_sum_day ");
        stringBuffer.append(" where room_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.dbUpdate();
    }

    @Override
    public List<RoomDetectionSumDay> getDayMaxTemperature(long roomId, String time, String temperature) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from room_detection_sum_day where room_id = ? and detection_date = ? and detection_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, time);
        db.set(3, temperature);
        List<RoomDetectionSumDay> list = db.dbQuery(RoomDetectionSumDay.class);
        if(list.size() == 0){
            return null;
        }
        return list;
    }
}
