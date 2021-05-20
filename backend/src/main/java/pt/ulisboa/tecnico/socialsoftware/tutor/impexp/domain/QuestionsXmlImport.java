package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_COURSE;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTIONS_IMPORT_ERROR;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_KEY_ALREADY_EXISTS;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_TYPE_NOT_IMPLEMENTED;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Languages;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CodeFillInQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CodeFillInSpotDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CodeOrderQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CodeOrderSlotDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.CourseRepository;

public class QuestionsXmlImport {
    public static final String CONTENT = "content";
    public static final String SEQUENCE = "sequence";
    private QuestionService questionService;
    private CourseRepository courseRepository;
    private CourseExecution loadCourseExecution;
    private List<QuestionDto> questions;

    public List<QuestionDto> importQuestions(InputStream inputStream, QuestionService questionService, CourseRepository courseRepository, CourseExecution loadCourseExecution) {
        this.questionService = questionService;
        this.courseRepository = courseRepository;
        this.loadCourseExecution = loadCourseExecution;
        this.questions = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);

        Document doc;
        try {
            Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (FileNotFoundException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File not found");
        } catch (JDOMException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "Coding problem");
        } catch (IOException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File type or format");
        }

        if (doc == null) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File not found ot format error");
        }

        importQuestions(doc);

        return questions;
    }

    public void importQuestions(String questionsXml, QuestionService questionService, CourseRepository courseRepository) {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);

        InputStream stream = new ByteArrayInputStream(questionsXml.getBytes());

        importQuestions(stream, questionService, courseRepository, null);
    }

    private void importQuestions(Document doc) {
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp = xpfac.compile("//questions/course", Filters.element());
        for (Element element : xp.evaluate(doc)) {
            importCourseQuestions(element);
        }
    }

    private void importCourseQuestions(Element courseElement) {
        String courseType = courseElement.getAttributeValue("courseType");
        String courseName = courseElement.getAttributeValue("courseName");

        Course course = courseRepository.findByNameType(courseName, courseType).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseName));
        if (loadCourseExecution != null && course != loadCourseExecution.getCourse()) {
            throw new TutorException(INVALID_COURSE, courseName + " " + courseType);
        }

        for (Element element : courseElement.getChildren()) {
            importQuestion(element, course);
        }
    }

    private void importQuestion(Element questionElement, Course course) {
        Integer key = null;
        if (loadCourseExecution == null) {
            key = Integer.valueOf(questionElement.getAttributeValue("key"));
            try {
                questionService.findQuestionByKey(key);
                throw new TutorException(QUESTION_KEY_ALREADY_EXISTS, key);
            } catch (TutorException tutorException) {
                // OK it does not exist
            }
        }
        String content = questionElement.getAttributeValue(CONTENT);
        String title = questionElement.getAttributeValue("title");
        String status = questionElement.getAttributeValue("status");
        String creationDate = questionElement.getAttributeValue("creationDate");
        String type = questionElement.getAttributeValue("type");

        QuestionDto questionDto = new QuestionDto();
        questionDto.setKey(key);
        questionDto.setContent(content);
        questionDto.setTitle(title);
        questionDto.setStatus(status);
        questionDto.setCreationDate(creationDate);

        Element imageElement = questionElement.getChild("image");
        if (imageElement != null) {
            Integer width = imageElement.getAttributeValue("width") != null ?
                    Integer.valueOf(imageElement.getAttributeValue("width")) : null;
            String url = imageElement.getAttributeValue("url");

            ImageDto imageDto = new ImageDto();
            imageDto.setWidth(width);
            imageDto.setUrl(url);

            questionDto.setImage(imageDto);
        }

        QuestionDetailsDto questionDetailsDto;
        switch (type) {
            case Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION:
                questionDetailsDto = importMultipleChoiceQuestion(questionElement);
                break;
            case Question.QuestionTypes.CODE_FILL_IN_QUESTION:
                questionDetailsDto = importCodeFillInQuestion(questionElement);
                break;
            case Question.QuestionTypes.CODE_ORDER_QUESTION:
                questionDetailsDto = importCodeOrderQuestion(questionElement);
                break;
            case Question.QuestionTypes.OPEN_ENDED_QUESTION:
                questionDetailsDto = importOpenEndedQuestion(questionElement);
                break;
            case Question.QuestionTypes.ITEM_COMBINATION_QUESTION:
                questionDetailsDto = importItemCombinationQuestion(questionElement);
                break;
            default:
                throw new TutorException(QUESTION_TYPE_NOT_IMPLEMENTED, type);
        }

        questionDto.setQuestionDetailsDto(questionDetailsDto);

        questions.add(questionService.createQuestion(course.getId(), questionDto));
    }

    private QuestionDetailsDto importMultipleChoiceQuestion(Element questionElement) {
        List<OptionDto> optionDtos = new ArrayList<>();
        for (Element optionElement : questionElement.getChild("options").getChildren("option")) {
            Integer optionSequence = Integer.valueOf(optionElement.getAttributeValue(SEQUENCE));
            String optionContent = optionElement.getAttributeValue(CONTENT);
            boolean correct = Boolean.parseBoolean(optionElement.getAttributeValue("correct"));
            Integer correct_order = Integer.valueOf(optionElement.getAttributeValue("correct_order"));

            OptionDto optionDto = new OptionDto();
            optionDto.setSequence(optionSequence);
            optionDto.setContent(optionContent);
            optionDto.setCorrect(correct);
            optionDto.setOrder(correct_order);

            optionDtos.add(optionDto);
        }
        MultipleChoiceQuestionDto multipleChoiceQuestionDto = new MultipleChoiceQuestionDto();
        multipleChoiceQuestionDto.setAnswerType(questionElement.getChild("answerTypeEl").getAttributeValue("answerType"));
        multipleChoiceQuestionDto.setOptions(optionDtos);
        return multipleChoiceQuestionDto;
    }

    private QuestionDetailsDto importCodeFillInQuestion(Element questionElement) {
        CodeFillInQuestionDto questionDto = new CodeFillInQuestionDto();
        questionDto.setCode(questionElement.getChildText("code"));
        questionDto.setLanguage(Languages.valueOf(questionElement.getChild("code").getAttributeValue("language")));
        var spots = new ArrayList<CodeFillInSpotDto>();
        for (Element spotElement : questionElement.getChild("fillInSpots").getChildren("fillInSpot")) {
            var spot = new CodeFillInSpotDto();
            spot.setSequence(Integer.valueOf(spotElement.getAttributeValue(SEQUENCE)));
            var options = new ArrayList<OptionDto>();
            for (Element optionElement : spotElement.getChildren("fillInOption")) {
                Integer optionSequence = Integer.valueOf(optionElement.getAttributeValue(SEQUENCE));
                String optionContent = optionElement.getAttributeValue(CONTENT);
                boolean correct = Boolean.parseBoolean(optionElement.getAttributeValue("correct"));

                OptionDto optionDto = new OptionDto();
                optionDto.setSequence(optionSequence);
                optionDto.setContent(optionContent);
                optionDto.setCorrect(correct);

                options.add(optionDto);
            }

            spot.setOptions(options);
            spots.add(spot);
        }
        questionDto.setFillInSpots(spots);
        return questionDto;
    }

    private QuestionDetailsDto importCodeOrderQuestion(Element questionElement) {
        CodeOrderQuestionDto questionDto = new CodeOrderQuestionDto();
        questionDto.setLanguage(Languages.valueOf(questionElement.getChild("orderSlots").getAttributeValue("language")));
        var slots = new ArrayList<CodeOrderSlotDto>();
        for (Element slotElement : questionElement.getChild("orderSlots").getChildren("slot")) {
            var slot = new CodeOrderSlotDto();

            Integer order = slotElement.getAttributeValue("order").equals("null") ? null : Integer.valueOf(slotElement.getAttributeValue("order"));
            slot.setOrder(order);
            slot.setSequence(Integer.valueOf(slotElement.getAttributeValue(SEQUENCE)));
            slot.setContent(slotElement.getValue());

            slots.add(slot);
        }
        questionDto.setCodeOrderSlots(slots);
        return questionDto;
    }

    private QuestionDetailsDto importOpenEndedQuestion(Element questionElement) {
        OpenEndedQuestionDto questionDto = new OpenEndedQuestionDto();
        questionDto.setCriteria(questionElement.getChildText("criteria"));
        questionDto.setRegexQuestion(questionElement.getChildText("regex"));
        return questionDto;
    }

    private QuestionDetailsDto importItemCombinationQuestion(Element questionElement) {
        List<CombinationItemDto> itemDtos = new ArrayList<>();
        List<List<Integer> > links = new ArrayList<>();
        for(Element itemElement: questionElement.getChild("items").getChildren("item")) {
            String itemGroup = itemElement.getAttributeValue("group");
            String itemContent = itemElement.getAttributeValue("content");
            String itemSequence = itemElement.getAttributeValue("sequence");
            CombinationItemDto itemDto = new CombinationItemDto();
            itemDto.setGroup(itemGroup);
            itemDto.setContent(itemContent);
            itemDto.setSequence(Integer.valueOf(itemSequence));
            itemDtos.add(itemDto);
            List<Integer> conns= new ArrayList<>();
            for(String s : itemElement.getAttributeValue("connections").split(",")) {
                if(!s.isBlank())
                    conns.add(Integer.valueOf(s));
            }
            links.add(conns);

        }
        ItemCombinationQuestionDto itemCombinationQuestionDto = new ItemCombinationQuestionDto();

        int index =0;
        for (List<Integer> c : links) {
            for (Integer seq : c) {
                itemDtos.get(index).addConnection(itemDtos.get(seq-1));
            }
            index++;
        }
        itemCombinationQuestionDto.setItems(itemDtos);

        return itemCombinationQuestionDto;
    }
}