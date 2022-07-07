package uz.itransition.collectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.itransition.collectin.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
