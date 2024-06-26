package com.fastcampus.fintechservice.db.lounge;


import com.fastcampus.fintechservice.common.error.UserErrorCode;
import com.fastcampus.fintechservice.common.exception.ApiException;
import com.fastcampus.fintechservice.db.AuditingFields;
import com.fastcampus.fintechservice.db.finance.enums.FinProductType;
import com.fastcampus.fintechservice.db.user.UserAccount;
import com.fastcampus.fintechservice.dto.request.LoungeRequest;
import com.fastcampus.fintechservice.dto.request.LoungeUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Lounge extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId")
    private Long id;
    @Column
    private String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserAccount user;

    @Column
    private String financialProduct1;
    @Column
    private String financialProduct2;

    @Column
    private String financialProduct1Name;

    @Column
    private String financialProduct2Name;

    @Column
    private String content;

    @Enumerated(EnumType.STRING)
    private FinProductType finProductType;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Column
    private int viewCount;

    @Column
    private int vote1;
    @Column
    private int vote2;


    public void loungeUpdate(LoungeUpdateRequest loungeUpdateRequest) {
        this.title = loungeUpdateRequest.getTitle();
        this.content = loungeUpdateRequest.getContent();
    }

    public void viewCount() {
        this.viewCount++;
    }


    public boolean validateUser(UserAccount user) {

        return !this.user.equals(user);
    }


}
