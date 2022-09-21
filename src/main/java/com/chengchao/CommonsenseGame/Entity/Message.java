package com.chengchao.CommonsenseGame.Entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@Document(indexName = "#{@IndexPrefixProvider.indexPrefix()}message")
public class Message {
    @Id
    private String id;

    /*  type 1 邀请进组
        type 2 邀请交换角色 */
    private Integer type;

    private String inviterPhone;
    private String inviterName;
    private String inviteePhone;
    private String inviteeName;
    private String groupName;
    private Integer role;

}
