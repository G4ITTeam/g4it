package com.soprasteria.g4it.backend.common.dbmodel;


import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

/**
 * Note  Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "note")
public class Note extends AbstractBaseEntity implements Serializable {

    /**
     * Primary Key : int64.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * note content.
     */
    private String content;

    /**
     * Attached user creator.
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;


    /**
     * The last user who updated the note.
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "last_updated_by", referencedColumnName = "id")
    private User lastUpdatedBy;

}
