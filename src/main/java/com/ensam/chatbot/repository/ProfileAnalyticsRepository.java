package com.ensam.chatbot.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfileAnalyticsRepository {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION = "profiles";

    public ProfileAnalyticsRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CompanyCount> topCurrentEmployers(Integer promoYear, String majorNorm, int topK) {
        Criteria matchBase = baseFilters(promoYear, majorNorm);

        Criteria expCriteria = Criteria.where("experiences.type").is("JOB")
                .and("experiences.jobStillWorking").is(true)
                .and("experiences.company").ne(null);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(matchBase),
                Aggregation.unwind("experiences"),
                Aggregation.match(expCriteria),
                Aggregation.group("experiences.company").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(topK)
        );

        return mongoTemplate.aggregate(agg, COLLECTION, CompanyCount.class).getMappedResults();
    }

    public List<CompanyCount> topCurrentJobTitles(Integer promoYear, String majorNorm, int topK) {
        Criteria matchBase = baseFilters(promoYear, majorNorm);

        Criteria expCriteria = Criteria.where("experiences.type").is("JOB")
                .and("experiences.jobStillWorking").is(true)
                .and("experiences.title").ne(null);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(matchBase),
                Aggregation.unwind("experiences"),
                Aggregation.match(expCriteria),
                Aggregation.group("experiences.title").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(topK)
        );

        return mongoTemplate.aggregate(agg, COLLECTION, CompanyCount.class).getMappedResults();
    }

    public List<CompanyCount> topSkills(Integer promoYear, String majorNorm, int topK) {
        Criteria matchBase = baseFilters(promoYear, majorNorm);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(matchBase),
                Aggregation.unwind("skills"),
                Aggregation.match(Criteria.where("skills").ne(null)),
                Aggregation.group("skills").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(topK)
        );

        return mongoTemplate.aggregate(agg, COLLECTION, CompanyCount.class).getMappedResults();
    }

    public List<CompanyCount> topPfeCompanies(Integer promoYear, String majorNorm, int topK) {
        Criteria matchBase = baseFilters(promoYear, majorNorm)
                .and("pfe.company").ne(null);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(matchBase),
                Aggregation.group("pfe.company").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(topK)
        );

        return mongoTemplate.aggregate(agg, COLLECTION, CompanyCount.class).getMappedResults();
    }

    // ---------------- helpers ----------------

    private Criteria baseFilters(Integer promoYear, String majorNorm) {
        Criteria c = new Criteria();

        if (promoYear != null) {
            c = c.and("mainEducation.endYear").is(promoYear);
        }
        if (majorNorm != null) {
            c = c.and("mainEducation.majorNorm").is(majorNorm);
        }

        return c;
    }
}
