package uz.itransition.collectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.itransition.collectin.entity.Tag;

public interface TagRepository extends JpaRepository<Tag,Long> {

}
