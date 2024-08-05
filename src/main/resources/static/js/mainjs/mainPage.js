$(document).ready(function() {
    let currentSlide = 0;
    const slides = $('.slide');
    const slideCount = slides.length;

    function showSlide(n) {
        slides.hide();
        slides.eq(n).show();
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % slideCount;
        showSlide(currentSlide);
    }

    function prevSlide() {
        currentSlide = (currentSlide - 1 + slideCount) % slideCount;
        showSlide(currentSlide);
    }

    showSlide(currentSlide);
    setInterval(nextSlide, 5000); // 5초마다 자동 슬라이드

    /* 인기 레시피 */
    function initializeSlider(section) {
        const prevBtn = section.find(".prev_btn");
        const nextBtn = section.find(".next_btn");
        const track = section.find(".slider_track");
        const cards = section.find(".recipe_card");
        let cardWidth = cards.outerWidth(true);
        const cardsPerPage = 6;

        cards.slice(-cardsPerPage).clone().prependTo(track);
        cards.slice(0, cardsPerPage).clone().appendTo(track);

        let currentPage = 1;
        track.css('transform', `translateX(-${cardsPerPage * cardWidth}px)`);

        function showCards() {
            const targetPosition = -((currentPage) * cardWidth * cardsPerPage);
            track.css({
                'transition': 'transform 0.5s ease',
                'transform': `translateX(${targetPosition}px)`
            });
        }

        prevBtn.click(function() {
            currentPage--;
            showCards();

            if (currentPage === 0) {
                setTimeout(() => {
                    track.css('transition', 'none');
                    currentPage = Math.ceil(cards.length / cardsPerPage);
                    track.css('transform', `translateX(-${currentPage * cardWidth * cardsPerPage}px)`);
                }, 500);
            }
        });

        nextBtn.click(function() {
            currentPage++;
            showCards();

            if (currentPage === Math.ceil(cards.length / cardsPerPage) + 1) {
                setTimeout(() => {
                    track.css('transition', 'none');
                    currentPage = 1;
                    track.css('transform', `translateX(-${currentPage * cardWidth * cardsPerPage}px)`);
                }, 500);
            }
        });

        $(window).resize(function() {
            cardWidth = cards.outerWidth(true);
            showCards();
        });
    }

    const sections = $(".recipe_section");
    sections.each(function() {
        initializeSlider($(this));
    });

    $('#userIcon').click(function(event) {
        event.preventDefault();
        event.stopPropagation();
        $('.user_two_menu').toggleClass('visible');
        $('.arrow-up').toggleClass('visible');
    });

    $(document).click(function(event) {
        if (!$(event.target).closest('#userIcon, .user_two_menu').length) {
            $('.user_two_menu').removeClass('visible');
            $('.arrow-up').removeClass('visible');
        }
    });

    $('.user_two_menu').click(function(event) {
        event.stopPropagation();
    });
});